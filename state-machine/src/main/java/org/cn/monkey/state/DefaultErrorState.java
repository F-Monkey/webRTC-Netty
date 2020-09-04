package org.cn.monkey.state;

public class DefaultErrorState extends StateAdapter {

    public static final String CODE = "ERROR";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public void update(Status status, ITime time) {
        // DO Noting
    }

    @Override
    public String finish(Status status, ITime time) {
        return null;
    }
}
