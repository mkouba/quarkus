package io.quarkus.scheduler.runtime.devconsole;

import java.time.Instant;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.scheduler.ScheduledExecution;
import io.quarkus.scheduler.Trigger;
import io.quarkus.scheduler.runtime.ScheduledInvoker;
import io.quarkus.scheduler.runtime.ScheduledMethodMetadata;
import io.quarkus.scheduler.runtime.SchedulerContext;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class SchedulerDevConsoleRecorder {

    public Handler<RoutingContext> invokeHandler() {
        // the usual issue of Vert.x hanging on to the first TCCL and setting it on all its threads
        final ClassLoader currentCl = Thread.currentThread().getContextClassLoader();
        return new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {
                event.request().setExpectMultipart(true);
                event.request().bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buf) {
                        try {
                            Thread.currentThread().setContextClassLoader(currentCl);
                            String name = event.request().formAttributes().get("name");
                            SchedulerContext context = Arc.container().instance(SchedulerContext.class).get();
                            for (ScheduledMethodMetadata i : context.getScheduledMethods()) {
                                if (i.getMethodDescription().equals(name)) {
                                    ScheduledInvoker invoker = context.createInvoker(i.getInvokerClassName());
                                    Instant now = Instant.now();
                                    invoker.invoke(new ScheduledExecution() {
                                        @Override
                                        public Trigger getTrigger() {
                                            return new Trigger() {

                                                @Override
                                                public String getId() {
                                                    return "dev-console";
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
                                    event.response().setStatusCode(204).end();
                                    return;
                                }
                            }
                            event.response().setStatusCode(404).end();
                        } catch (Throwable t) {
                            t.printStackTrace();
                            event.response().setStatusCode(500).end();
                        }
                    }
                });
            }
        };
    }
}
