package com.demo.mybatis.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class HttpResult<T> {
    private boolean success;
    private String msg;
    private T data;

    public HttpResult(){
        this.success=true;
        this.msg="成功";
    }

    public HttpResult(T t){
        this.success=true;
        this.msg="成功";
        this.data = t;
    }

    public HttpResult(boolean success, String msg){
        this.success=success;
        this.msg=msg;
    }

}
