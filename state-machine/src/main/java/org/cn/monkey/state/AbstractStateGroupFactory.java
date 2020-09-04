package org.cn.monkey.state;

import org.cn.monkey.common.thread.AutoTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class AbstractStateGroupFactory implements IStateGroupFactory {

    private static final Logger log = LoggerFactory.getLogger(AbstractStateGroupFactory.class);

    protected final int roomCapacity;

    protected volatile ConcurrentHashMap<Long, IStateGroup> stateGroupMap;

    public AbstractStateGroupFactory(int roomCapacity) {
        this.roomCapacity = roomCapacity;
        this.stateGroupMap = new ConcurrentHashMap<>();
        new AutoTask("roomStateGroupFactory", 1, 1, TimeUnit.MINUTES) {
            @Override
            protected void run() {
                AbstractStateGroupFactory.this.removeDeposedStateGroup();
            }
        }.start();
    }

    protected void removeDeposedStateGroup() {
        ConcurrentHashMap<Long, IStateGroup> stateGroupMap = new ConcurrentHashMap<>(this.stateGroupMap);
        stateGroupMap.entrySet().removeIf(e -> {
            IStateGroup stateGroup = e.getValue();
            if (stateGroup.canDepose()) {
                log.info("stateGroup: {} has bean removed", stateGroup.getId());
                return true;
            }
            return false;
        });
        this.stateGroupMap = stateGroupMap;
    }

    @Override
    public IStateGroup find(long roomId) {
        return this.stateGroupMap.get(roomId);
    }
}
