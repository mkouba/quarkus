package io.quarkus.deployment.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public final class ArtifactInfoUtil {

    /**
     * Returns a Map.Entry containing the groupId and the artifactId of the module the contains the BuildItem
     *
     * The way this works is by depending on the pom.properties file that should be present in the deployment jar
     */
    public static Map.Entry<String, String> groupIdAndArtifactId(Class<?> clazz) {
        try {
            URL jarLocation = clazz.getProtectionDomain().getCodeSource().getLocation();
            try (FileSystem fs = FileSystems.newFileSystem(Paths.get(jarLocation.toURI()),
                    Thread.currentThread().getContextClassLoader())) {
                Path metaInfPath = fs.getPath("/META-INF");
                Optional<Path> pomProperties = Files.walk(metaInfPath)
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith("pom.properties"))
                        .findFirst();
                if (pomProperties.isPresent()) {
                    Properties props = new Properties();
                    props.load(Files.newInputStream(pomProperties.get()));
                    return new AbstractMap.SimpleEntry<>(props.getProperty("groupId"), props.getProperty("artifactId"));
                } else {
                    throw new RuntimeException("Unable to determine groupId and artifactId of the jar that contains "
                            + clazz.getName() + " because the jar doesn't contain the necessary metadata");
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Unable to determine groupId and artifactId of the jar that contains " + clazz.getName(),
                    e);
        }
    }
}
