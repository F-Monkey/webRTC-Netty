package org.cn.monkey.state;

import com.google.common.base.Preconditions;
import org.cn.monkey.common.thread.AutoTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class WorkerManager implements IWorkerManager {

    private static final Logger log = LoggerFactory.getLogger(WorkerManager.class);

    private final AtomicLong ID_COUNT = new AtomicLong(1);

    private final int updateWaitTime;

    private final int stateGroupCapacity;

    private static final int DEFAULT_STATE_GROUP_CAPACITY = 1 << 3;

    private volatile ConcurrentHashMap<Long, IWorker> workerMap;

    public WorkerManager(int updateWaitTime, int stateGroupCapacity) {
        this.workerMap = new ConcurrentHashMap<>();
        Preconditions.checkArgument(updateWaitTime > 0);
        Preconditions.checkArgument(stateGroupCapacity >= DEFAULT_STATE_GROUP_CAPACITY);
        this.updateWaitTime = updateWaitTime;
        this.stateGroupCapacity = stateGroupCapacity;
        new AutoTask("workerManager", 1, 1, TimeUnit.MINUTES) {
            @Override
            protected void run() {
                WorkerManager.this.removeWorker();
            }
        }.start();
    }


    @Override
    public IWorker addStateGroup(final IStateGroup stateGroup) {
        ConcurrentHashMap<Long, IWorker> workerMap = this.workerMap;
        IWorker workerTemp = null;
        for (IWorker worker : workerMap.values()) {

            if (!worker.isStarted()) {
                continue;
            }
            
            if (worker.tryAddOneStateGroup(stateGroup)) {
                workerTemp = worker;
                break;
            }
        }

        if (workerTemp == null) {
            long id = ID_COUNT.getAndIncrement();

            workerTemp = workerMap.computeIfAbsent(id, (key) -> {
                        StateGroupWorker stateGroupWorker = new StateGroupWorker(key, this.updateWaitTime, this.stateGroupCapacity);
                        stateGroupWorker.start();
                        return stateGroupWorker;
                    }
            );
            workerTemp.tryAddOneStateGroup(stateGroup);
            this.workerMap = workerMap;
        }
        return workerTemp;
    }

    private void removeWorker() {
        ConcurrentHashMap<Long, IWorker> workerMap = new ConcurrentHashMap<>(this.workerMap);
        workerMap.entrySet().removeIf(e -> {
            IWorker worker = e.getValue();
            if (!worker.isStarted()) {
                return true;
            }

            if (worker.isEmpty()) {
                worker.stop();
                return true;
            }
            return false;
        });
        this.workerMap = workerMap;
    }
}
