package io.quarkus.devconsole.spi;

import io.quarkus.builder.item.MultiBuildItem;

/**
 * Information that can be directly displayed in dev console templates, using the info: prefix
 *
 * This is scoped to the extension that produced it, to prevent namespace clashes
 */
public final class TemplateInfoBuildItem extends MultiBuildItem {

    private final String groupId;
    private final String artifactId;
    private final String name;
    private final Object object;

    public TemplateInfoBuildItem(String groupId, String artifactId, String name, Object object) {
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

    public Object getObject() {
        return object;
    }
}
