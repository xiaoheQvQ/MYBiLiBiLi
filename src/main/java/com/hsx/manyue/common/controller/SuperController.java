package com.hsx.manyue.common.controller;

import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.enums.ReturnCodeEnums;

/**
 * 父类控制器
 *
 */
public class SuperController {

    /**
     * 成功响应
     *
     * @return
     */
    public R success() {
        return R.success();
    }

    /**
     * 成功响应
     *
     * @param data
     * @return
     */
    public R success(Object data) {
        return R.success(data);
    }

    /**
     * 失败响应
     *
     * @return
     */
    public R failure() {
        return R.failure();
    }

    /**
     * 失败响应
     *
     * @param returnCode
     * @return
     */
    public R failure(ReturnCodeEnums returnCode) {
        return R.failure(returnCode);
    }
}
