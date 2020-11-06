package io.quarkus.container.image.deployment.devconsole;

import java.util.Map;

import io.quarkus.bootstrap.app.CuratedApplication;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.dev.console.TempSystemProperties;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class RebuildHandler implements Handler<RoutingContext> {

    private final Map<String, String> config;

    public RebuildHandler(Map<String, String> config) {
        this.config = config;
    }

    @Override
    public void handle(RoutingContext event) {
        event.request().setExpectMultipart(true);
        event.request().endHandler(new Handler<Void>() {
            @Override
            public void handle(Void ignore) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        QuarkusBootstrap existing = (QuarkusBootstrap) DevConsoleManager.getQuarkusBootstrap();
                        try (TempSystemProperties properties = new TempSystemProperties()) {
                            for (Map.Entry<String, String> i : config.entrySet()) {
                                properties.set(i.getKey(), i.getValue());
                            }
                            for (Map.Entry<String, String> i : event.request().formAttributes().entries()) {
                                properties.set(i.getKey(), i.getValue());
                            }
                            QuarkusBootstrap quarkusBootstrap = existing.clonedBuilder()
                                    .setMode(QuarkusBootstrap.Mode.PROD)
                                    .setIsolateDeployment(true).build();
                            try (CuratedApplication bootstrap = quarkusBootstrap.bootstrap()) {
                                bootstrap
                                        .createAugmentor().createProductionApplication();
                                event.response().end("Deployment Complete"); //TODO: FIXME, should give template output
                            }
                        } catch (Exception e) {
                            event.response().setStatusCode(500).end();
                            e.printStackTrace();
                        }
                    }
                }, "Container build thread").start();
            }
        });
    }

}
