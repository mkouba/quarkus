package io.quarkus.arc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.enterprise.inject.spi.InterceptionType;
import javax.interceptor.InvocationContext;

/**
 * This object is not thread-safe and should never be used concurrently. Hovewer, the state should be safely propagated accross
 * threads.
 *
 * @author Martin Kouba
 */
public class InvocationContextImpl implements InvocationContext {

    public static final String KEY_INTERCEPTOR_BINDINGS = "io.quarkus.arc.interceptorBindings";

    /**
     *
     * @param target
     * @param method
     * @param args
     * @param chain
     * @param aroundInvokeForward
     * @param interceptorBindings
     * @return a new {@link javax.interceptor.AroundInvoke} invocation context
     */
    public static InvocationContextImpl aroundInvoke(Object target, Method method, Object[] args,
            List<InterceptorInvocation> chain,
            Function<InvocationContext, Object> aroundInvokeForward, Set<Annotation> interceptorBindings) {
        return new InvocationContextImpl(target, method, null, args, chain, aroundInvokeForward, null, interceptorBindings);
    }

    /**
     *
     * @param target
     * @param chain
     * @param interceptorBindings
     * @return a new {@link javax.annotation.PostConstruct} invocation context
     */
    public static InvocationContextImpl postConstruct(Object target, List<InterceptorInvocation> chain,
            Set<Annotation> interceptorBindings) {
        return new InvocationContextImpl(target, null, null, null, chain, null, null, interceptorBindings);
    }

    /**
     *
     * @param target
     * @param chain
     * @param interceptorBindings
     * @return a new {@link javax.annotation.PreDestroy} invocation context
     */
    public static InvocationContextImpl preDestroy(Object target, List<InterceptorInvocation> chain,
            Set<Annotation> interceptorBindings) {
        return new InvocationContextImpl(target, null, null, null, chain, null, null, interceptorBindings);
    }

    /**
     *
     * @param target
     * @param chain
     * @param interceptorBindings
     * @return a new {@link javax.interceptor.AroundConstruct} invocation context
     */
    public static InvocationContextImpl aroundConstruct(Constructor<?> constructor, List<InterceptorInvocation> chain,
            Supplier<Object> aroundConstructForward,
            Set<Annotation> interceptorBindings) {
        return new InvocationContextImpl(null, null, constructor, null, chain, null, aroundConstructForward,
                interceptorBindings);
    }

    private final AtomicReference<Object> target;

    private final Method method;

    private final Constructor<?> constructor;

    private final List<Object> args;

    private final AtomicInteger position;

    private final Map<String, Object> contextData;

    private final List<InterceptorInvocation> chain;

    private final Function<InvocationContext, Object> aroundInvokeForward;

    private final Supplier<Object> aroundConstructForward;

    private final Set<Annotation> interceptorBindings;

    /**
     * @param target
     * @param method
     * @param constructor
     * @param args
     * @param chain
     * @param aroundInvokeForward
     * @param aroundConstructForward
     * @param interceptorBindings
     */
    InvocationContextImpl(Object target, Method method, Constructor<?> constructor, Object[] args,
            List<InterceptorInvocation> chain,
            Function<InvocationContext, Object> aroundInvokeForward, Supplier<Object> aroundConstructForward,
            Set<Annotation> interceptorBindings) {
        this.target = new AtomicReference<>(target);
        this.method = method;
        this.constructor = constructor;
        this.args = args != null ? new CopyOnWriteArrayList<>(args) : new CopyOnWriteArrayList<>();
        this.position = new AtomicInteger(0);
        this.chain = chain;
        this.aroundInvokeForward = aroundInvokeForward;
        this.aroundConstructForward = aroundConstructForward;
        this.interceptorBindings = interceptorBindings;
        contextData = new HashMap<>();
        contextData.put(KEY_INTERCEPTOR_BINDINGS, interceptorBindings);
    }

    boolean hasNextInterceptor() {
        return position.get() < chain.size();
    }

    protected Object invokeNext() throws Exception {
        try {
            return chain.get(position.getAndIncrement()).invoke(this);
        } finally {
            position.decrementAndGet();
        }
    }

    protected Object interceptorChainCompleted() throws Exception {
        if (aroundInvokeForward != null) {
            return aroundInvokeForward.apply(this);
        }
        if (aroundConstructForward != null) {
            target.set(aroundConstructForward.get());
        }
        return null;
    }

    @Override
    public Object proceed() throws Exception {
        try {
            if (hasNextInterceptor()) {
                if (aroundConstructForward != null) {
                    invokeNext();
                    return target.get();
                } else {
                    return invokeNext();
                }
            } else {
                // The last interceptor in chain called proceed()
                return interceptorChainCompleted();
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw new RuntimeException(cause);
        }
    }

    @Override
    public Object getTarget() {
        return target.get();
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Constructor<?> getConstructor() {
        return constructor;
    }

    @Override
    public Object[] getParameters() throws IllegalStateException {
        if (args == null) {
            throw new IllegalStateException();
        }
        return args.toArray();
    }

    @Override
    public void setParameters(Object[] params) throws IllegalStateException, IllegalArgumentException {
        if (args == null) {
            throw new IllegalStateException();
        }
        // This is not very effective but the method is not called very often 
        this.args.clear();
        Collections.addAll(this.args, params);
    }

    @Override
    public Map<String, Object> getContextData() {
        return contextData;
    }

    @Override
    public Object getTimer() {
        return null;
    }

    public Set<Annotation> getInterceptorBindings() {
        return interceptorBindings;
    }

    public static class InterceptorInvocation {

        public static InterceptorInvocation aroundInvoke(InjectableInterceptor<?> interceptor, Object interceptorInstance) {
            return new InterceptorInvocation(InterceptionType.AROUND_INVOKE, interceptor, interceptorInstance);
        }

        public static InterceptorInvocation postConstruct(InjectableInterceptor<?> interceptor, Object interceptorInstance) {
            return new InterceptorInvocation(InterceptionType.POST_CONSTRUCT, interceptor, interceptorInstance);
        }

        public static InterceptorInvocation preDestroy(InjectableInterceptor<?> interceptor, Object interceptorInstance) {
            return new InterceptorInvocation(InterceptionType.PRE_DESTROY, interceptor, interceptorInstance);
        }

        public static InterceptorInvocation aroundConstruct(InjectableInterceptor<?> interceptor, Object interceptorInstance) {
            return new InterceptorInvocation(InterceptionType.AROUND_CONSTRUCT, interceptor, interceptorInstance);
        }

        private final InterceptionType interceptionType;

        @SuppressWarnings("rawtypes")
        private final InjectableInterceptor interceptor;

        private final Object interceptorInstance;

        InterceptorInvocation(InterceptionType interceptionType, InjectableInterceptor<?> interceptor,
                Object interceptorInstance) {
            this.interceptionType = interceptionType;
            this.interceptor = interceptor;
            this.interceptorInstance = interceptorInstance;
        }

        @SuppressWarnings("unchecked")
        Object invoke(InvocationContext ctx) throws Exception {
            return interceptor.intercept(interceptionType, interceptorInstance, ctx);
        }

    }

}
