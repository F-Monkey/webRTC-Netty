package org.cn.monkey.common.thread;

import org.cn.monkey.common.utils.LogUtil;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AutoTask {

    private static final Logger log = LoggerFactory.getLogger(AutoTask.class);

    private final ScheduledExecutorService scheduledExecutorService;

    private final String name;

    private final long delay;

    private final long period;

    private final TimeUnit timeUnit;

    public AutoTask(String name, long delay, long period, TimeUnit timeUnit) {
        Preconditions.checkNotNull(timeUnit);
        this.name = name;
        this.delay = delay;
        this.period = period;
        this.timeUnit = timeUnit;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(2);
    }

    public String getName() {
        return this.name;
    }

    public final void start() {
        this.autoRun();
    }

    final void autoRun() {
        try {
            this.scheduledExecutorService
                    .scheduleAtFixedRate(this::run, this.delay, this.period, this.timeUnit);
        } catch (Exception e) {
            log.error("name: {}, autoRun error, exception:\n{}", this.getName(), LogUtil.stackTrace(e));
        }
    }

    protected abstract void run();
}
