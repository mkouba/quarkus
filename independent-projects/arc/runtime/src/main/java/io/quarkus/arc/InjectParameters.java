package io.quarkus.arc;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

/**
 * The parameters of the intercepted method will be replaced by contextual instances.
 */
@InterceptorBinding
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
@Documented
public @interface InjectParameters {

    /**
     * Skip injection for a parameter.
     */
    @Target(PARAMETER)
    @Retention(RUNTIME)
    @interface Skip {

    }

}
