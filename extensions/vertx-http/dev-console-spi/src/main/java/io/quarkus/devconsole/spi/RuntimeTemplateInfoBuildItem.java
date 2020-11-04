package io.quarkus.devconsole.spi;

import java.util.function.Supplier;

import io.quarkus.builder.item.MultiBuildItem;

/**
 * Information that can be directly displayed in dev console templates, using the info: prefix
 *
 * This is scoped to the extension that produced it, to prevent namespace clashes.
 *
 * This value will be evaluated at runtime, so can contain info that is produced from recorders
 */
public final class RuntimeTemplateInfoBuildItem extends MultiBuildItem {

    private final String groupId;
    private final String artifactId;
    private final String name;
    private final Supplier<? extends Object> object;

    public RuntimeTemplateInfoBuildItem(String groupId, String artifactId, String name, Supplier<? extends Object> object) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.name = name;
        this.object = object;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getName() {
        return name;
    }

    public Supplier<? extends Object> getObject() {
        return object;
    }
}
