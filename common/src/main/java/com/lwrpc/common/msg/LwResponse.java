package com.lwrpc.common.msg;

import java.io.Serializable;

public class LwResponse implements Serializable{
    private String requestId;
    private Object result;
    private Throwable cause;

    public boolean isError() {
        return cause != null;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }
}

