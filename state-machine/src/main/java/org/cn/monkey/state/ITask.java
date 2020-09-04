package org.cn.monkey.state;

public interface ITask {
    void run();

    void runOnError(Throwable error);
}
