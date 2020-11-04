package io.quarkus.vertx.http.deployment.devmode;

import org.jboss.logging.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.netty.runtime.virtual.VirtualChannel;
import io.quarkus.netty.runtime.virtual.VirtualServerChannel;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.runtime.devmode.DevConsoleFilter;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.impl.Http1xServerConnection;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.impl.VertxHandler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class DevConsoleProcessor {

    private static final Logger log = Logger.getLogger(DevConsoleProcessor.class);
    protected static volatile ServerBootstrap virtualBootstrap;
    protected static volatile Vertx devConsoleVertx;
    protected static volatile Channel channel;
    static Router router;

    public static void initializeVirtual() {
        if (virtualBootstrap != null) {
            return;
        }
        devConsoleVertx = Vertx.vertx();
        VertxInternal vertx = (VertxInternal) devConsoleVertx;
        QuarkusClassLoader ccl = (QuarkusClassLoader) DevConsoleProcessor.class.getClassLoader();
        ccl.addCloseTask(new Runnable() {
            @Override
            public void run() {
                virtualBootstrap = null;
                if (devConsoleVertx != null) {
                    devConsoleVertx.close();
                    devConsoleVertx = null;
                }
                if (channel != null) {
                    try {
                        channel.close().sync();
                    } catch (InterruptedException e) {
                        throw new RuntimeException("failed to close virtual http");
                    }
                }
            }
        });
        router = Router.router(vertx);
        router.route().handler(new DevConsole());
        Handler<RoutingContext> errorHandler = new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {
                log.error("Dev console request failed ", event.failure());
            }
        };
        router.errorHandler(500, errorHandler);
        virtualBootstrap = new ServerBootstrap();
        virtualBootstrap.group(vertx.getEventLoopGroup())
                .channel(VirtualServerChannel.class)
                .handler(new ChannelInitializer<VirtualServerChannel>() {
                    @Override
                    public void initChannel(VirtualServerChannel ch) throws Exception {
                        //ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                    }
                })
                .childHandler(new ChannelInitializer<VirtualChannel>() {
                    @Override
                    public void initChannel(VirtualChannel ch) throws Exception {
                        ContextInternal context = (ContextInternal) vertx
                                .createEventLoopContext(null, null, new JsonObject(),
                                        Thread.currentThread().getContextClassLoader());
                        VertxHandler<Http1xServerConnection> handler = VertxHandler.create(context, chctx -> {
                            Http1xServerConnection conn = new Http1xServerConnection(
                                    context.owner(),
                                    null,
                                    new HttpServerOptions(),
                                    chctx,
                                    context,
                                    "localhost",
                                    null);
                            Router mainRouter = Router.router(vertx);
                            mainRouter.errorHandler(500, errorHandler);
                            mainRouter.route("/@dev/*").subRouter(router);
                            conn.handler(mainRouter);
                            return conn;
                        });
                        ch.pipeline().addLast("handler", handler);
                    }
                });

        // Start the server.
        try {
            ChannelFuture future = virtualBootstrap.bind(DevConsoleHttpHandler.QUARKUS_DEV_CONSOLE);
            future.sync();
            channel = future.channel();
        } catch (InterruptedException e) {
            throw new RuntimeException("failed to bind virtual http");
        }

    }

    @BuildStep(onlyIf = IsDevelopment.class)
    public RouteBuildItem setupBuildStep(LaunchModeBuildItem launchModeBuildItem) {
        if (!launchModeBuildItem.getLaunchMode().isDevOrTest()) {
            return null;
        }
        initializeVirtual();
        DevConsoleManager.registerHandler(new DevConsoleHttpHandler());
        return new RouteBuildItem("/@dev/*", new DevConsoleFilter());

    }

}
