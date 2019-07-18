package io.quarkus.arc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.inject.Qualifier;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import io.quarkus.arc.InjectParameters.Skip;

@Interceptor
@InjectParameters
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 100)
public class MethodParamsInjectionInterceptor {

    private final Annotation[] EMPTY = new Annotation[] {};

    @AroundInvoke
    Object aroundInvoke(InvocationContext ctx) throws Exception {
        Type[] paramTypes = ctx.getMethod().getGenericParameterTypes();
        Object[] params = new Object[paramTypes.length];
        Annotation[][] paramAnnotations = ctx.getMethod().getParameterAnnotations();
        List<InstanceHandle<?>> dependentHandles = new LinkedList<>();
        ArcContainer container = Arc.container();
        for (int i = 0; i < paramTypes.length; i++) {
            Annotation[] annotations = paramAnnotations[i];
            if (skip(annotations)) {
                params[i] = ctx.getParameters()[i];
            } else {
                InstanceHandle<?> handle = container.instance(paramTypes[i], getQualifiers(annotations));
                if (handle.getBean() != null && handle.getBean().getScope().equals(Dependent.class)) {
                    dependentHandles.add(handle);
                }
                params[i] = handle.isAvailable() ? handle.get() : null;
            }
        }
        ctx.setParameters(params);
        Object ret;
        try {
            ret = ctx.proceed();
        } finally {
            if (!dependentHandles.isEmpty()) {
                for (InstanceHandle<?> handle : dependentHandles) {
                    handle.destroy();
                }
            }
        }
        return ret;
    }

    private Annotation[] getQualifiers(Annotation[] annotations) {
        List<Annotation> qualifiers = new LinkedList<>();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                qualifiers.add(annotation);
            }
        }
        return qualifiers.isEmpty() ? EMPTY : qualifiers.toArray(EMPTY);
    }

    private boolean skip(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Skip.class)) {
                return true;
            }
        }
        return false;
    }

}
