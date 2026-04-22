package io.quarkus.arc.test.invoker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.constant.ClassDesc;
import java.util.function.Supplier;

import jakarta.enterprise.invoke.Invoker;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.deployment.BeanDiscoveryFinishedBuildItem;
import io.quarkus.arc.deployment.InvokerFactoryBuildItem;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.arc.processor.InvokerBuilder;
import io.quarkus.arc.processor.InvokerInfo;
import io.quarkus.builder.BuildContext;
import io.quarkus.builder.BuildStep;
import io.quarkus.deployment.BuildProducerImpl;
import io.quarkus.deployment.GeneratedClassGizmo2Adaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.builditem.GeneratedClassBuildItem;
import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.test.QuarkusExtensionTest;
import io.smallrye.mutiny.Uni;

public class InvokerBuilderTransformerTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .withApplicationRoot(root -> root
                    .addClasses(SimpleBean.class))
            .addBuildChainCustomizer(b -> {
                b.addBuildStep(new BuildStep() {
                    @Override
                    public void execute(BuildContext context) {
                        InvokerFactoryBuildItem invokerFactory = context.consume(InvokerFactoryBuildItem.class);
                        BeanDiscoveryFinishedBuildItem beanDiscovery = context.consume(BeanDiscoveryFinishedBuildItem.class);
                        BeanInfo bean = beanDiscovery.beanStream().withBeanType(SimpleBean.class).firstResult().get();
                        MethodInfo ping = bean.getTarget().get().asClass().method("ping");
                        InvokerBuilder invokerBuilder = invokerFactory.createInvoker(bean, ping)
                                .withInstanceLookup()
                                .withReturnValueTransformer(SimpleBean.class, "toUni");
                        InvokerInfo invoker = invokerBuilder.build();
                        ClassDesc invokerClass = invoker.getClassDesc();
                        BuildProducer<GeneratedClassBuildItem> generatedClasses = new BuildProducerImpl<>(
                                GeneratedClassBuildItem.class, context);
                        ClassOutput classOutput = new GeneratedClassGizmo2Adaptor(generatedClasses, null, true);
                        Gizmo gizmo = Gizmo.create(classOutput);
                        gizmo.class_("io.quarkus.arc.test.invoker.SimpleBeanInvoker", cc -> {
                            cc.defaultConstructor();
                            cc.implements_(Supplier.class);
                            cc.method("get", mc -> {
                                mc.returning(Invoker.class);
                                mc.body(bc -> {
                                    Expr inv = bc.new_(invokerClass);
                                    bc.return_(inv);
                                });
                            });
                        });
                    }
                }).consumes(InvokerFactoryBuildItem.class)
                        .consumes(BeanDiscoveryFinishedBuildItem.class)
                        .produces(GeneratedClassBuildItem.class)
                        .build();
            });

    @Inject
    SimpleBean simpleBean;

    @SuppressWarnings("unchecked")
    @Test
    public void testBeans() throws Exception {
        Supplier<Object> simpleBeanInvoker = (Supplier<Object>) Thread.currentThread().getContextClassLoader()
                .loadClass("io.quarkus.arc.test.invoker.SimpleBeanInvoker").getConstructor().newInstance();
        Invoker<?, ?> invoker = (Invoker<?, ?>) simpleBeanInvoker.get();
        Object ret = invoker.invoke(null, new Object[] {});
        if (ret instanceof Uni uni) {
            Object val = uni.await().indefinitely();
            if (val instanceof Integer) {
                assertEquals(42, val);
            } else {
                fail("Val is not Integer");
            }
        } else {
            fail("Ret is not Uni");
        }
    }

    @Singleton
    static class SimpleBean {

        public int ping() {
            return 42;
        }

        static <T> Uni<T> toUni(T val) {
            return Uni.createFrom().item(val);
        }

    }
}
