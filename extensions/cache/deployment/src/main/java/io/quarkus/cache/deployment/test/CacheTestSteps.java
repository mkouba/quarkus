package io.quarkus.cache.deployment.test;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.cache.runtime.CacheTestStateImpl;
import io.quarkus.deployment.IsTest;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;

@BuildSteps(onlyIf = IsTest.class)
public class CacheTestSteps {

    @BuildStep
    public void additionalBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(CacheTestStateImpl.class));
    }

}
