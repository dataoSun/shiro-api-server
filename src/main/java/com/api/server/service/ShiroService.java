
package com.api.server.service;


import java.util.Set;

/**
 * shiro相关接口
 *
 */
public interface ShiroService {
    /**
     * 获取用户权限列表
     */
    Set<String> getUserPermissions(long userId);


}
