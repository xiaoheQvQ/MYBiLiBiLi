package com.hsx.manyue.common.utils;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class PageUtils {

    public static <T, R> IPage<R> convert(IPage<T> page, Class<R> targetClass) {
        Page<R> result = new Page(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(BeanUtil.copyToList(page.getRecords(), targetClass));
        return result;
    }
}
