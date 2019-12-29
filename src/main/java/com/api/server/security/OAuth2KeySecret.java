
package com.api.server.security;

import lombok.Builder;
import lombok.Data;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * 生成key Secret
 *
 */
@Builder
@Data
public class OAuth2KeySecret implements AuthenticationToken {
    /**
     * api key
      */
    private String key;

    /**
     * api sign
      */
    private String sign;

    /**
     *  request uri
     */
    private String uri;

    /**
     * request payload
     */
    private String query;


    public OAuth2KeySecret(String key, String sign, String uri, String query) {
        this.key = key;
        this.sign = sign;
        this.uri = uri;
        this.query = query;
    }

    @Override
    public Object getPrincipal() {
        return sign;
    }

    @Override
    public Object getCredentials() {
        return sign;
    }

}
