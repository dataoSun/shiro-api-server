package com.api.server.config;

import com.api.client.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import retrofit2.http.*;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * api service, check request is legal
 */
@Slf4j
@Component
public class ApiServiceConfig {

    private static Method[] methods;

    private static Map<Method, Annotation[]> annotationMap = new HashMap<>(16);

    private static Map<String, String> authenticationLeverMap = new HashMap<>(16);

    private static Map<String, String> checkUrlMethodMap = new HashMap<>(16);

    private static final String EMPTY_STR = new String();

    @PostConstruct
    private void init() throws Exception {
        // 获取所有methods
        findMthods();
        // 获取所有注解
        findAnnotations();
        // save
        transformMap();

    }

    private void transformMap() {
        if (null == annotationMap || annotationMap.isEmpty()){
            return;
        }
        for (Map.Entry<Method, Annotation[]> entry : annotationMap.entrySet()){
            // 解析 method
            analysisMethod(entry);
        }

    }

    private void analysisMethod(Map.Entry<Method,Annotation[]> entry) {
        if (Objects.isNull(entry) || Objects.isNull(entry.getValue())) return;
        Annotation[] annotations = entry.getValue();
        String requestUrl = EMPTY_STR;
        StringBuilder authenticationLever =  new StringBuilder();
        for (Annotation annotation : annotations){
            // 解析 annotation
            requestUrl = analysisAnnotationUrl(annotation);
            authenticationLever.append(analysisAnnotationHeader(annotation));
        }
        if (StringUtils.isNotBlank(requestUrl)){
            authenticationLeverMap.put(requestUrl, authenticationLever.toString());
        }
    }

    private String analysisAnnotationHeader(Annotation annotation) {
        if (Objects.isNull(annotation) ) return EMPTY_STR;
        String[] lever = new String[]{};
        if (annotation instanceof Headers){
            lever = ((Headers) annotation).value();
        }
        if (null == lever || lever.length <= 0) return EMPTY_STR;
        return StringUtils.join(lever, ",");
    }

    private String analysisAnnotationUrl( Annotation annotation) {
        if (Objects.isNull(annotation)) return EMPTY_STR;
        String url;
        String urlMethod;
        if (annotation instanceof GET){
            url = ((GET) annotation).value();
            urlMethod = HttpMethod.GET.name();
        }else if( annotation instanceof POST){
            url = ((POST) annotation).value();
            urlMethod = HttpMethod.POST.name();
        }else if (annotation instanceof PUT){
            url = ((PUT) annotation).value();
            urlMethod = HttpMethod.PUT.name();
        }else if (annotation instanceof DELETE){
            url = ((DELETE) annotation).value();
            urlMethod = HttpMethod.DELETE.name();
        }else {
            return EMPTY_STR;
        }
        if (StringUtils.isBlank(url)) return EMPTY_STR;
        url = url.replaceFirst("(/|\\\\)+", "");
        checkUrlMethodMap.put(url, urlMethod);
        return url;
    }

    private void findAnnotations() {
        if (Objects.isNull(methods)){
            return;
        }
        for (Method method : methods){
            Annotation[] annotations = method.getAnnotations();
            if (Objects.isNull(annotations)) continue;
            annotationMap.put(method, annotations);
        }
    }

    private void findMthods() throws ClassNotFoundException {
        Class<ApiService> serviceClass = ApiService.class;
        methods = serviceClass.getDeclaredMethods();
    }

    /** ---------------------------------------check---------------------------------------------- */

    public static String findAuthenticationLever(String requestUrl){
        requestUrl = fixUri(requestUrl);
        return authenticationLeverMap.get(requestUrl);
    }

    public static boolean checkUrl(String requestUrl){
        if (StringUtils.isBlank(requestUrl)){
            return false;
        }
        requestUrl = fixUri(requestUrl);
        return checkUrlMethodMap.containsKey(requestUrl);
    }

    private static String fixUri(String requestUrl) {
        requestUrl = requestUrl.replaceFirst("(/|\\\\)+", "");
        return requestUrl;
    }

    public static boolean checkUrlMode(String requestUrl, String mode){
        if (StringUtils.isBlank(requestUrl) || StringUtils.isBlank(mode)){
            return false;
        }
        requestUrl = fixUri(requestUrl);
        String trueMode = checkUrlMethodMap.get(requestUrl);
        if (StringUtils.isBlank(trueMode)) {
            return false;
        }
        if (!StringUtils.equalsIgnoreCase(trueMode, StringUtils.trim(mode))) {
            return false;
        }
        return true;
    }


}
