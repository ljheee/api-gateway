package com.ljheee.api.service;

import java.io.Serializable;

/**
 * Created by lijianhua04 on 2018/8/24.
 */
public class Goods implements Serializable{
    private String code;
    private int size;

    public Goods(String code, int size) {
        this.code = code;
        this.size = size;
    }

    public Goods() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "Goods{" +
                "code='" + code + '\'' +
                ", size=" + size +
                '}';
    }
}