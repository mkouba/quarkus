package io.quarkus.arc.test;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.builder.BuildContext;
import io.quarkus.builder.BuildStep;
import io.quarkus.deployment.builditem.RunTimeConfigurationDefaultBuildItem;
import io.quarkus.test.QuarkusUnitTest;

public class LoggingConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withEmptyApplication()
            .addBuildChainCustomizer(b -> {
                b.addBuildStep(new BuildStep() {
                    @Override
                    public void execute(BuildContext context) {
                        context.produce(new RunTimeConfigurationDefaultBuildItem("quarkus.log.console.enable", "false"));
                    }
                }).produces(RunTimeConfigurationDefaultBuildItem.class).build();
            });

    @Test
    public void testConsoleLogging() {
        System.out.println(ConfigProvider.getConfig().getConfigValue("quarkus.log.console.enable"));
    }

}
