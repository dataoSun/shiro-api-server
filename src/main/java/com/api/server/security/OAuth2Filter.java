
package com.api.server.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.api.client.R;
import com.api.client.constants.ApiConstants;
import com.api.server.config.ApiServiceConfig;
import com.api.server.utils.HttpContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * oauth2过滤器
 *
 */
@Slf4j
public class OAuth2Filter extends AuthenticatingFilter {

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        if (judgeApiUrl(servletRequest.getRequestURI())){
            return OAuth2KeySecret.builder()
                    .key(servletRequest.getHeader(ApiConstants.API_KEY_HEADER))
                    .sign(servletRequest.getHeader(ApiConstants.API_SIGN_HEADER))
                    .uri(servletRequest.getRequestURI())
                    .query(servletRequest.getQueryString())
                    .build();
        }

        //获取请求token
        String token = getRequestToken((HttpServletRequest) request);

        if(StrUtil.isBlank(token)){
            return null;
        }

        return new OAuth2Token(token);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if(((HttpServletRequest) request).getMethod().equals(RequestMethod.OPTIONS.name())){
            return true;
        }

        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        //获取请求token，如果token不存在，直接返回401
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // 先判断url是否是api
        if (judgeApiUrl(httpServletRequest.getRequestURI())){
            return checkSign(httpServletRequest, httpResponse);
        }
        String token = getRequestToken(httpServletRequest);
        if(StrUtil.isBlank(token)){

            errorResponse(R.error(HttpStatus.HTTP_UNAUTHORIZED, "invalid token"), httpResponse);

            return false;
        }

        return executeLogin(request, response);
    }

    private void errorResponse(R r, HttpServletResponse httpResponse) throws IOException {
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());

        String json = JSONUtil.toJsonStr(r);

        httpResponse.getWriter().print(json);
    }


    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setContentType("application/json;charset=utf-8");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());
        try {
            //处理登录失败的异常
            Throwable throwable = e.getCause() == null ? e : e.getCause();
            R r = R.error(HttpStatus.HTTP_UNAUTHORIZED, throwable.getMessage());

            String json = JSONUtil.toJsonStr(r);
            httpResponse.getWriter().print(json);
        } catch (IOException e1) {

        }

        return false;
    }

    /**
     * 获取请求的token
     */
    private String getRequestToken(HttpServletRequest httpRequest){

        //从header中获取token
        String token = httpRequest.getHeader("token");

        //如果header中不存在token，则从参数中获取token
        if(StrUtil.isBlank(token)){
            token = httpRequest.getParameter("token");
        }

        return token;
    }

    /**
     *  判断是否是对外开放api
     * @param requestURI
     * @return
     */
    private boolean judgeApiUrl(String requestURI) {
        return ApiServiceConfig.checkUrl(requestURI);
    }

    /**
     * check secret sign
     * @param request
     * @return
     */
    private boolean checkSign(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uri = request.getRequestURI();

        String apiKey = request.getHeader(ApiConstants.API_KEY_HEADER);

        String sign = request.getHeader(ApiConstants.API_SIGN_HEADER);

        String requestMethod = request.getMethod();

        if (!ApiServiceConfig.checkUrlMode(uri, requestMethod)){
            response(HttpStatus.HTTP_UNAUTHORIZED , "method not allowed!", response);
            return false;
        }

        String headers = ApiServiceConfig.findAuthenticationLever(uri);
        if (null == headers){
            return executeLogin(request, response);
        }

        if (!authentication(request, response, apiKey, sign, headers)){
            return false;
        }

        return executeLogin(request, response);
    }

    private boolean authentication(HttpServletRequest request, HttpServletResponse response
            , String apiKey, String sign, String headers) {
        if(headers.contains(ApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED_HEADER)){
            if (StrUtil.isBlank(apiKey) || StrUtil.isBlank(sign)){
                return response(500, "apiKey和sign不能为空", response);
            }
        }else {
            if (StrUtil.isBlank(apiKey)){
                return response(500, "apiKey不能为空", response);
            }
        }

        return true;
    }

    private boolean response(Integer code,String msg, HttpServletResponse response) {
        try {
            errorResponse(R.error(code, msg), response);
        } catch (IOException e) {

        }
        return false;
    }


}
