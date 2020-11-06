package io.quarkus.container.image.deployment.devconsole;

import java.util.Collections;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devconsole.spi.DevConsoleRouteBuildItem;

public class ContainerImageDevConsoleProcessor {

    @BuildStep
    DevConsoleRouteBuildItem builder() {
        return new DevConsoleRouteBuildItem("build", "POST",
                new RebuildHandler(Collections.singletonMap("quarkus.container-image.build", "true")));
    }
}
