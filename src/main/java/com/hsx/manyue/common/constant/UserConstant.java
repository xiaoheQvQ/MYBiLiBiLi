package com.hsx.manyue.common.constant;

import java.util.ArrayList;
import java.util.List;

public class UserConstant {

    public static final String GENDER_MALE = "0";

    public static final String GENDER_FEMALE = "1";

    public static final String GENDER_UNKNOW = "2";

    public static final String DEFAULT_BIRTH = "2000-01-01";

    public static final String DEFAULT_NICK = "萌新";

    public static final String DEFAULT_AVATAR = "https://mp-a29d2492-cdac-4c3b-89f4-f4d0e8bd7b62.cdn.bspapp.com/仿b站/默认配置/默认头像.jpg";

    public static final String DEFAULT_SIGN = "暂未设置签名";

    public static final String USER_FOLLOWING_GROUP_TYPE_DEFAULT = "2";

    public static final String USER_FOLLOWING_GROUP_ALL_NAME = "全部分组";

    public static final String USER_FOLLOWING_GROUP_TYPE_USER = "3";


    /** 头像文件大小的上限值(10MB) */
    public static final int AVATAR_MAX_SIZE = 10 * 1024 * 1024;
    /** 允许上传的头像的文件类型 */
    List<String> AVATAR_TYPES = new ArrayList<String>(){{
        add("png");
        add("jpg");
    }};

}
