package io.quarkus.scheduler.runtime;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.cronutils.model.CronType;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.scheduler.ScheduledExecution;
import io.quarkus.scheduler.Trigger;

@Recorder
public class SchedulerRecorder {

    public Supplier<Object> createContext(SchedulerConfig config, ExecutorService executorService,
            List<ScheduledMethodMetadata> scheduledMethods) {
        return new Supplier<Object>() {
            @Override
            public Object get() {
                return new SchedulerContext() {

                    @Override
                    public ExecutorService getExecutor() {
                        return executorService;
                    }

                    @Override
                    public CronType getCronType() {
                        return config.cronType;
                    }

                    @Override
                    public List<ScheduledMethodMetadata> getScheduledMethods() {
                        return scheduledMethods;
                    }
                };
            }
        };
    }

    public Consumer<String> invokerForMethod() {
        return new Consumer<String>() {
            @Override
            public void accept(String s) {
                SchedulerContext context = Arc.container().instance(SchedulerContext.class).get();
                for (ScheduledMethodMetadata method : context.getScheduledMethods()) {
                    if (method.getMethodDescription().equals(s)) {
                        Instant now = Instant.now();
                        context.createInvoker(method.getInvokerClassName()).invoke(new ScheduledExecution() {
                            @Override
                            public Trigger getTrigger() {
                                return new SimpleScheduler.SimpleTrigger("fake", null) {
                                    @Override
                                    ZonedDateTime evaluate(ZonedDateTime now) {
                                        return null;
                                    }

                                    @Override
                                    public Instant getNextFireTime() {
                                        return null;
                                    }

                                    @Override
                                    public Instant getPreviousFireTime() {
                                        return now;
                                    }
                                };
                            }

                            @Override
                            public Instant getFireTime() {
                                return now;
                            }

                            @Override
                            public Instant getScheduledFireTime() {
                                return now;
                            }
                        });
                    }
                }
            }
        };
    }

}
