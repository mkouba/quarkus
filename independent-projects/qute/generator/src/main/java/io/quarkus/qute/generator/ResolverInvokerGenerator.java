package io.quarkus.qute.generator;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

import java.util.concurrent.CompletionStage;

import org.jboss.jandex.IndexView;

import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.qute.EvalContext;
import io.quarkus.qute.Resolver;
import io.quarkus.qute.ResolverInvoker;

public class ResolverInvokerGenerator extends AbstractGenerator {

    public ResolverInvokerGenerator(IndexView index, ClassOutput classOutput) {
        super(index, classOutput);
    }

    /**
     *
     * @param resolverClasses
     * @return the fully qualified name of the generated class
     */
    public String generate(Iterable<String> resolverClasses) {

        String generatedName = "io.quarkus.qute.GeneratedResolverInvoker";
        ClassCreator resolverInvoker = ClassCreator.builder().classOutput(classOutput).className(generatedName)
                .superClass(ResolverInvoker.class).build();

        MethodCreator resolve = resolverInvoker
                .getMethodCreator("resolve", CompletionStage.class, Resolver.class, EvalContext.class)
                .setModifiers(ACC_PUBLIC);

        for (String resolverClass : resolverClasses) {
            BytecodeCreator isInstance = resolve.ifTrue(resolve.instanceOf(resolve.getMethodParam(0), resolverClass))
                    .trueBranch();
            ResultHandle cast = isInstance.checkCast(resolve.getMethodParam(0), resolverClass);
            isInstance.returnValue(isInstance.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(resolverClass, "resolve", CompletionStage.class, EvalContext.class), cast,
                    resolve.getMethodParam(1)));
        }

        // return super.resolve(resolver, context);
        resolve.returnValue(resolve.invokeSpecialMethod(
                MethodDescriptor.ofMethod(ResolverInvoker.class, "resolve", CompletionStage.class,
                        Resolver.class, EvalContext.class),
                resolve.getThis(), resolve.getMethodParam(0), resolve.getMethodParam(1)));

        resolverInvoker.close();

        return generatedName;
    }

}
