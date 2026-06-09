package com.flower.base;

import lombok.Data;

import java.util.List;

@Data
public class PageBean<T> {
    private List<T> records;
    private long total;
    private int pageNum;
    private int pageSize;

    public PageBean() {
    }

    public PageBean(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}