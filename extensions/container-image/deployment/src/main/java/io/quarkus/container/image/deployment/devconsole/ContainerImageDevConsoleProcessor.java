package io.quarkus.container.image.deployment.devconsole;

import io.quarkus.bootstrap.BootstrapException;
import io.quarkus.bootstrap.app.QuarkusBootstrap;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.dev.console.TempSystemProperties;
import io.quarkus.devconsole.spi.DevConsoleRouteBuildItem;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;


public class ContainerImageDevConsoleProcessor {

    @BuildStep
    DevConsoleRouteBuildItem builder() {
        return new DevConsoleRouteBuildItem("build", "POST", new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        QuarkusBootstrap existing = (QuarkusBootstrap) DevConsoleManager.getQuarkusBootstrap();
                        try (TempSystemProperties properties = new TempSystemProperties()) {
                            properties.set("quarkus.container-image.build", "true");
                            existing.clonedBuilder().setMode(QuarkusBootstrap.Mode.PROD).setIsolateDeployment(true).build()
                                    .bootstrap()
                                    .createAugmentor().createProductionApplication();
                            event.response().end("Container Image Created"); //TODO: FIXME, should give template output
                        } catch (BootstrapException e) {
                            event.response().setStatusCode(500).end();
                            e.printStackTrace();
                        }
                    }
                }, "Container build thread").start();
            }
        });
    }
}
