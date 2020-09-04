package org.cn.monkey.state;

public interface IWorker {

    long getId();

    boolean tryAddOneStateGroup(IStateGroup stateGroup);

    void start();

    boolean isStarted();

    void stop();

    void update();

    boolean isEmpty();
}
