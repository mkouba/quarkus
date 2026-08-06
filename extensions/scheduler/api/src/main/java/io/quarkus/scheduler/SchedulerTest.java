package io.quarkus.scheduler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.interceptor.InterceptorBinding;

/**
 * Interceptor binding that controls the {@link Scheduler} lifecycle around {@code @QuarkusTest}
 * methods. When placed on a test class, the scheduler is paused during test instance construction (so that
 * {@code @InjectMock} has a chance to install mocks before any {@code @Scheduled} method can run) and resumed for the
 * duration of each test method.
 * <p>
 * Note that the corresponding interceptor is only registered in test mode.
 */
@InterceptorBinding
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerTest {

}
