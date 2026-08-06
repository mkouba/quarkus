package io.quarkus.scheduler.runtime.test;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduler;
import io.quarkus.scheduler.SchedulerTest;

@Interceptor
@SchedulerTest
public class SchedulerTestInterceptor {

    private static final Logger LOG = Logger.getLogger(SchedulerTestInterceptor.class);

    @Inject
    Scheduler scheduler;

    @AroundConstruct
    public Object interceptConstructor(InvocationContext ctx) throws Exception {
        boolean paused = false;
        if (scheduler.isRunning()) {
            // Pause the scheduler so that no scheduled method can run before @InjectMock installs the mocks
            scheduler.pause();
            paused = true;
        }
        try {
            return ctx.proceed();
        } finally {
            if (paused) {
                LOG.infof("Paused running scheduler when constructing %s", toIdentityString(ctx.getTarget()));
            }
        }
    }

    @AroundInvoke
    public Object interceptTestMethod(InvocationContext ctx) throws Exception {
        // Mocks are installed; resume the scheduler for the duration of the test method
        scheduler.resume();
        LOG.infof("Resumed scheduler before %s", ctx.getTarget() + "#" + ctx.getMethod().getName() + "()");
        try {
            return ctx.proceed();
        } finally {
            scheduler.pause();
            LOG.infof("Paused scheduler after %s", ctx.getTarget() + "#" + ctx.getMethod().getName() + "()");
        }
    }

    private static String toIdentityString(Object o) {
        return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
    }

}
