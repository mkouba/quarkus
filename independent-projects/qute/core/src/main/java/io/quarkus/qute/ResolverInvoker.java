package io.quarkus.qute;

import java.util.concurrent.CompletionStage;

import io.quarkus.qute.ValueResolvers.ArrayResolver;
import io.quarkus.qute.ValueResolvers.CollectionResolver;
import io.quarkus.qute.ValueResolvers.ListResolver;
import io.quarkus.qute.ValueResolvers.LogicalAndResolver;
import io.quarkus.qute.ValueResolvers.LogicalOrResolver;
import io.quarkus.qute.ValueResolvers.MapEntryResolver;
import io.quarkus.qute.ValueResolvers.MapResolver;
import io.quarkus.qute.ValueResolvers.MapperResolver;
import io.quarkus.qute.ValueResolvers.MinusResolver;
import io.quarkus.qute.ValueResolvers.ModResolver;
import io.quarkus.qute.ValueResolvers.NumberValueResolver;
import io.quarkus.qute.ValueResolvers.OrEmptyResolver;
import io.quarkus.qute.ValueResolvers.OrResolver;
import io.quarkus.qute.ValueResolvers.PlusResolver;
import io.quarkus.qute.ValueResolvers.RawResolver;
import io.quarkus.qute.ValueResolvers.ThisResolver;
import io.quarkus.qute.ValueResolvers.TrueResolver;

/**
 * TODO
 */
public class ResolverInvoker {

    public CompletionStage<Object> resolve(Resolver resolver, EvalContext context) {
        if (resolver instanceof ReflectionValueResolver reflect) {
            return reflect.resolve(context);
        }
        if (resolver instanceof RawResolver raw) {
            return raw.resolve(context);
        }
        if (resolver instanceof ListResolver list) {
            return list.resolve(context);
        }
        if (resolver instanceof CollectionResolver collection) {
            return collection.resolve(context);
        }
        if (resolver instanceof ThisResolver this_) {
            return this_.resolve(context);
        }
        if (resolver instanceof OrResolver or) {
            return or.resolve(context);
        }
        if (resolver instanceof OrEmptyResolver orEmpty) {
            return orEmpty.resolve(context);
        }
        if (resolver instanceof TrueResolver true_) {
            return true_.resolve(context);
        }
        if (resolver instanceof MapEntryResolver mapEntry) {
            return mapEntry.resolve(context);
        }
        if (resolver instanceof MapResolver map) {
            return map.resolve(context);
        }
        if (resolver instanceof MapperResolver mapper) {
            return mapper.resolve(context);
        }
        if (resolver instanceof LogicalAndResolver and) {
            return and.resolve(context);
        }
        if (resolver instanceof LogicalOrResolver or) {
            return or.resolve(context);
        }
        if (resolver instanceof ArrayResolver array) {
            return array.resolve(context);
        }
        if (resolver instanceof NumberValueResolver number) {
            return number.resolve(context);
        }
        if (resolver instanceof PlusResolver plus) {
            return plus.resolve(context);
        }
        if (resolver instanceof MinusResolver minus) {
            return minus.resolve(context);
        }
        if (resolver instanceof ModResolver mod) {
            return mod.resolve(context);
        }
        return resolver.resolve(context);
    }

}
