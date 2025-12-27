package com.hsx.manyue.common.dto;


import lombok.Data;

@Data
public class PageParam {

    private long current;
    private long size;

    public void setCurrent(long current) {
        this.current = current <= 0 ? 1 : current;
    }

    public void setSize(long size) {
        this.size = size <= 0 ? 20 : size;
    }
}
