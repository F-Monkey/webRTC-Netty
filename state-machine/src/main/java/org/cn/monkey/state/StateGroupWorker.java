package org.cn.monkey.state;

import org.cn.monkey.common.thread.AutoTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

class StateGroupWorker implements IWorker {

    private static final Logger log = LoggerFactory.getLogger(StateGroupWorker.class);

    private static final float DEFAULT_FACTOR = 0.75f;

    private final Object LOCK = new Object();

    private final long id;

    private final int updateWaitTime;

    private final int stateGroupCapacity;

    private volatile ConcurrentHashMap<Long, IStateGroup> stateGroupMap;

    private volatile boolean started = false;

    private Thread workerThread;

    public StateGroupWorker(long id, int updateWaitTime, int stateGroupCapacity) {
        this.id = id;
        this.updateWaitTime = updateWaitTime;
        this.stateGroupCapacity = stateGroupCapacity;
        this.stateGroupMap = new ConcurrentHashMap<>(this.stateGroupCapacity);
        new AutoTask("stateGroupWorker_" + this.id, 1, 1, TimeUnit.MINUTES) {
            @Override
            protected void run() {
                StateGroupWorker.this.removeDeposedGroup();
            }
        }.start();
    }

    private void removeDeposedGroup() {
        ConcurrentHashMap<Long, IStateGroup> stateGroupMap = new ConcurrentHashMap<>(this.stateGroupMap);
        stateGroupMap.entrySet().removeIf(e -> e.getValue().canDepose());
        this.stateGroupMap = stateGroupMap;
    }

    private void run() {
        for (; ; ) {
            synchronized (this.LOCK) {
                try {
                    Thread.sleep(this.updateWaitTime);
                    if (this.stateGroupMap.size() <= 0) {
                        this.LOCK.wait();
                    }
                } catch (InterruptedException ignore) {
                }
                this.update();
            }
        }
    }


    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public boolean tryAddOneStateGroup(IStateGroup stateGroup) {
        if (this.stateGroupMap.size() >= this.stateGroupCapacity * DEFAULT_FACTOR) {
            return false;
        }

        this.stateGroupMap.computeIfPresent(stateGroup.getId(), (k, v) -> {
            if (v.canDepose()) {
                return stateGroup;
            }
            return v;
        });

        synchronized (this.LOCK) {
            this.LOCK.notifyAll();
        }
        return true;
    }

    @Override
    public void start() {
        if (this.workerThread != null) {
            if (this.workerThread.isAlive()) {
                this.workerThread.interrupt();
            }
        }
        this.workerThread = new Thread(this::run);
        this.workerThread.start();
        this.started = true;
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    @Override
    public void stop() {
        this.started = false;
        if (this.workerThread != null) {
            if (!this.workerThread.isInterrupted()) {
                this.workerThread.interrupt();
            }
        }
    }

    @Override
    public void update() {
        ConcurrentHashMap<Long, IStateGroup> stateGroupMap = this.stateGroupMap;
        for (IStateGroup stateGroup : stateGroupMap.values()) {
            if (!stateGroup.canDepose()) {
                stateGroup.update();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return this.stateGroupMap.isEmpty();
    }
}
