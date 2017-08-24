package com.tessoft.mykaraoke;

/**
 * Created by Daeyong on 2017-08-22.
 */
public class APIResponse {

    private String resCode = "0000";
    private String resMsg = "SUCCESS";

    private Object data = null;

    public String getResCode() {
        return resCode;
    }

    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    public String getResMsg() {
        return resMsg;
    }

    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

