//package com.api.server.entity;
//
//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
//import com.baomidou.mybatisplus.extension.activerecord.Model;
//import lombok.Builder;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.experimental.Accessors;
//
//import java.io.Serializable;
//
///**
// *
// */
//@Builder
//@Data
//@EqualsAndHashCode(callSuper = false)
//@Accessors(chain = true)
//@TableName("platform")
//public class Platform extends Model<Platform> {
//
//    private static final long serialVersionUID=1L;
//
//    @TableId(value = "id", type = IdType.AUTO)
//    private int id;
//
//    /**
//     * 平台名称
//     */
//    private String name;
//
//    /**
//     * 平台描述
//     */
//    private String description;
//
//    /**
//     * api key
//     */
//    private String apiKey;
//
//    /**
//     * api secret
//     */
//    private String apiSecret;
//
//    /**
//     * 创建时间
//     */
//    private Long createTime;
//
//    /**
//     * 更新时间
//     */
//    private Long updateTime;
//
//
//    public static final String ID = "id";
//
//    public static final String NAME = "name";
//
//    public static final String DESCRIPTION = "description";
//
//    public static final String API_KEY = "api_key";
//
//    public static final String API_SECRET = "api_secret";
//
//    public static final String CREATE_TIME = "create_time";
//
//    public static final String UPDATE_TIME = "update_time";
//
//    @Override
//    protected Serializable pkVal() {
//        return this.id;
//    }
//
//}
