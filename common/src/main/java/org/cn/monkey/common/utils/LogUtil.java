package org.cn.monkey.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtil {
    private LogUtil() {
    }

    public static String stackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
