package com.minesweeper.Domain;

import lombok.Data;

@Data
public class Res {
    private String msg;
    private Object data;
    private Integer code;
    public Res(String msg, Object data, Integer code) {
        this.msg = msg;
        this.data = data;
        this.code = code;
    }
    static public Res success() {
        return new Res("success", null, 200);
    }
    static public Res success(Object data) {
        return new Res("success", data, 200);
    }
    static public Res success(String msg,Object data) {
        return new Res(msg, data, 200);
    }
    static public Res fail() {
        return new Res("fail", null, 400);
    }
    static public Res fail(String msg) {
        return new Res(msg, null, 400);
    }
    static public Res fail(String msg, Object data) {
        return new Res(msg, data, 400);
    }

}
