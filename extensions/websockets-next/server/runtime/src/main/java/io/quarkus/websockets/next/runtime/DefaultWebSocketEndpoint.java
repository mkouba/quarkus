package io.quarkus.websockets.next.runtime;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jboss.logging.Logger;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.virtual.threads.VirtualThreadsRecorder;
import io.quarkus.websockets.next.WebSocket.ExecutionMode;
import io.quarkus.websockets.next.WebSocketRuntimeConfig;
import io.quarkus.websockets.next.WebSocketServerConnection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;

public abstract class DefaultWebSocketEndpoint implements WebSocketEndpoint {

    private static final Logger LOG = Logger.getLogger(DefaultWebSocketEndpoint.class);

    protected final WebSocketServerConnection connection;

    protected final Codecs codecs;

    private final ConcurrencyLimiter limiter;

    private final WebSocketRuntimeConfig config;

    public DefaultWebSocketEndpoint(Context context, WebSocketServerConnection connection, Codecs codecs,
            WebSocketRuntimeConfig config) {
        this.connection = connection;
        this.codecs = codecs;
        this.limiter = executionMode() == ExecutionMode.SERIAL ? new ConcurrencyLimiter(context, connection) : null;
        this.config = config;
    }

    @Override
    public Future<Void> onOpen(Context context) {
        return execute(context, null, onOpenExecutionModel(), this::doOnOpen);
    }

    @Override
    public Future<Void> onMessage(Context context, Object message) {
        return execute(context, message, onMessageExecutionModel(), this::doOnMessage);
    }

    @Override
    public Future<Void> onClose(Context context) {
        return execute(context, null, onCloseExecutionModel(), this::doOnClose);
    }

    private Future<Void> execute(Context context, Object message, ExecutionModel executionModel,
            BiFunction<Context, Object, Uni<Void>> action) {
        Promise<Void> promise = Promise.promise();
        if (limiter != null) {
            Consumer<Void> complete = limiter.newComplete(promise);
            limiter.run(new Runnable() {
                @Override
                public void run() {
                    if (executionModel == ExecutionModel.VIRTUAL_THREAD) {
                        VirtualThreadsRecorder.getCurrent().execute(new Runnable() {
                            @Override
                            public void run() {
                                withTimeout(action.apply(context, message)).subscribe().with(complete);
                            }
                        });
                    } else if (executionModel == ExecutionModel.WORKER_THREAD) {
                        context.executeBlocking(new Callable<Void>() {
                            @Override
                            public Void call() {
                                withTimeout(action.apply(context, message)).subscribe().with(complete);
                                return null;
                            }
                        }, false);
                    } else {
                        withTimeout(action.apply(context, message)).subscribe().with(complete);
                    }
                }
            });
        } else {
            if (executionModel == ExecutionModel.VIRTUAL_THREAD) {
                VirtualThreadsRecorder.getCurrent().execute(new Runnable() {
                    @Override
                    public void run() {
                        withTimeout(action.apply(context, message)).subscribe().with(promise::complete);
                    }
                });
            } else if (executionModel == ExecutionModel.WORKER_THREAD) {
                context.executeBlocking(new Callable<Void>() {
                    @Override
                    public Void call() {
                        withTimeout(action.apply(context, message)).subscribe().with(promise::complete);
                        return null;
                    }
                }, false);
            } else {
                withTimeout(action.apply(context, message)).subscribe().with(promise::complete);
            }
        }
        return promise.future();
    }

    private Uni<Void> withTimeout(Uni<Void> action) {
        if (config.timeout().isEmpty()) {
            return action;
        }
        return action.ifNoItem().after(config.timeout().get()).fail();
    }

    protected Object beanInstance(String identifier) {
        ArcContainer container = Arc.container();
        return container.instance(container.bean(identifier)).get();
    }

    protected Uni<Void> doOnOpen(Context context, Object message) {
        return Uni.createFrom().voidItem();
    }

    protected Uni<Void> doOnMessage(Context context, Object message) {
        return Uni.createFrom().voidItem();
    }

    protected Uni<Void> doOnClose(Context context, Object message) {
        return Uni.createFrom().voidItem();
    }

    protected Object decodeText(Type type, String value, Class<?> codecBeanClass) {
        return codecs.textDecode(type, value, codecBeanClass);
    }

    protected String encodeText(Object value, Class<?> codecBeanClass) {
        if (value == null) {
            return null;
        }
        return codecs.textEncode(value, codecBeanClass);
    }

    protected Object decodeBinary(Type type, Buffer value, Class<?> codecBeanClass) {
        return codecs.binaryDecode(type, value, codecBeanClass);
    }

    protected Buffer encodeBinary(Object value, Class<?> codecBeanClass) {
        if (value == null) {
            return null;
        }
        return codecs.binaryEncode(value, codecBeanClass);
    }

    protected Uni<Void> sendText(String message, boolean broadcast) {
        return broadcast ? connection.broadcast().sendText(message) : connection.sendText(message);
    }

    protected Uni<Void> multiText(Multi<Object> multi, boolean broadcast, Function<Object, Uni<Void>> itemFun) {
        multi.onFailure().call(connection::close).subscribe().with(
                m -> {
                    itemFun.apply(m).subscribe().with(v -> LOG.debugf("Multi >> text message: %s", connection),
                            t -> LOG.errorf(t, "Unable to send text message from Multi: %s", connection));
                });
        return Uni.createFrom().voidItem();
    }

    protected Uni<Void> sendBinary(Buffer message, boolean broadcast) {
        return broadcast ? connection.broadcast().sendBinary(message) : connection.sendBinary(message);
    }

    protected Uni<Void> multiBinary(Multi<Object> multi, boolean broadcast, Function<Object, Uni<Void>> itemFun) {
        multi.onFailure().call(connection::close).subscribe().with(
                m -> {
                    itemFun.apply(m).subscribe().with(v -> LOG.debugf("Multi >> binary message: %s", connection),
                            t -> LOG.errorf(t, "Unable to send binary message from Multi: %s", connection));
                });
        return Uni.createFrom().voidItem();
    }
}
