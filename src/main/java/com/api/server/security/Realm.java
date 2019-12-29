//
//package com.api.server.security;
//
//import cn.hutool.core.util.StrUtil;
//import com.api.client.constants.ApiConstants;
//import com.api.client.security.HmacSHA256Signer;
//import com.api.server.config.ApiServiceConfig;
//import com.api.server.entity.Platform;
//import com.api.server.entity.SysUser;
//import com.api.server.exception.ASException;
//import com.api.server.service.ShiroService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.shiro.authc.*;
//import org.apache.shiro.authz.AuthorizationInfo;
//import org.apache.shiro.authz.SimpleAuthorizationInfo;
//import org.apache.shiro.realm.AuthorizingRealm;
//import org.apache.shiro.subject.PrincipalCollection;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URLDecoder;
//import java.util.Set;
//
///**
// * 认证
// */
//@Slf4j
//@Component
//public class Realm extends AuthorizingRealm {
//    @Autowired
//    private ShiroService shiroService;
//    @Autowired
//    private PlatformService platformService;
//
//    @Override
//    public boolean supports(AuthenticationToken token) {
//        return token instanceof OAuth2Token || token instanceof OAuth2KeySecret;
//    }
//
//    /**
//     * 授权(验证权限时调用)
//     */
//    @Override
//    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
//        SysUser user = (SysUser)principals.getPrimaryPrincipal();
//        Long userId = user.getUserId();
//
//        //用户权限列表
//        Set<String> permsSet = shiroService.getUserPermissions(userId);
//
//        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
//        info.setStringPermissions(permsSet);
//        return info;
//    }
//
//    /**
//     * 认证(登录时调用)
//     */
//    @Override
//    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
//
//        if (token instanceof OAuth2KeySecret) {
//            Platform platform = authentication((OAuth2KeySecret) token);
//            SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(platform, token.getPrincipal(), getName());
//            return info;
//        }
//
//        String accessToken = (String) token.getPrincipal();
//
//        //根据accessToken，查询用户信息
//        SysUserToken tokenEntity = shiroService.queryByToken(accessToken);
//        //token失效
//        if(tokenEntity == null || tokenEntity.getExpireTime().getTime() < System.currentTimeMillis()){
//            throw new IncorrectCredentialsException("token失效，请重新登录");
//        }
//
//        //查询用户信息
//        SysUser user = shiroService.queryUser(tokenEntity.getUserId());
//        //账号锁定
//        if(user.getStatus() == 0){
//            throw new LockedAccountException("账号已被锁定,请联系管理员");
//        }
//
//        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, accessToken, getName());
//        return info;
//    }
//
//    private Platform authentication(OAuth2KeySecret oAuth2KeySecret) {
//        String apiKey = oAuth2KeySecret.getKey();
//        String query = oAuth2KeySecret.getQuery();
//        String sign = oAuth2KeySecret.getSign();
//        String uri = oAuth2KeySecret.getUri();
//        String headers = ApiServiceConfig.findAuthenticationLever(uri);
//        if (StrUtil.isBlank(headers)){
//            Platform platform = Platform.builder()
//                    .id(-1)
//                    .build();
//            return platform;
//        }
//        Platform platform = platformService.getOne(new QueryWrapper<Platform>().eq(Platform.API_KEY, apiKey));
//        if (null == platform){
//            throw new ASException("apiKey无效", 500);
//        }
//        if(headers.contains(ApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED_HEADER)){
//
//            try {
//                query=URLDecoder.decode(query, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                throw new ASException("编码格式错误", 500);
//            }
//            String confirmSign = "";
//            try {
//                confirmSign = HmacSHA256Signer.sign(query, platform.getApiSecret());
//            }catch (Exception e){
//                log.error("签名异常:", e);
//                throw new ASException("签名错误", 500);
//            }
//            if (!StrUtil.equals(confirmSign, sign)){
//                throw new ASException("签名错误", 500);
//            }
//        }
//        return platform;
//    }
//
//
//
//}
