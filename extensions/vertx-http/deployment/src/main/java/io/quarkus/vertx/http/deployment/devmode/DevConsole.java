package io.quarkus.vertx.http.deployment.devmode;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.BiFunction;

import org.yaml.snakeyaml.Yaml;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.qute.Engine;
import io.quarkus.qute.NamespaceResolver;
import io.quarkus.qute.ReflectionValueResolver;
import io.quarkus.qute.Results;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.TemplateLocator;
import io.quarkus.qute.ValueResolvers;
import io.quarkus.qute.Variant;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * This is a Handler running in the Dev Vert.x instance (which is loaded by the Augmentation ClassLoader)
 * and has access to build time stuff
 */
public class DevConsole implements Handler<RoutingContext> {

    private static final ThreadLocal<String> currentExtension = new ThreadLocal<>();

    static final Engine engine = Engine.builder().addDefaultSectionHelpers().addDefaultValueResolvers()
            .addValueResolver(new ReflectionValueResolver())
            .addValueResolver(ValueResolvers.rawResolver())
            .addNamespaceResolver(NamespaceResolver.builder("info").resolve(ctx -> {
                String ext = currentExtension.get();
                if (ext == null) {
                    return Results.Result.NOT_FOUND;
                }
                Map<String, Object> map = DevConsoleManager.getTemplateInfo().get(ext);
                if (map == null) {
                    return Results.Result.NOT_FOUND;
                }
                Object result = map.get(ctx.getName());
                return result == null ? Results.Result.NOT_FOUND : result;
            }).build())
            .addLocator(id -> locateTemplate(id))
            .build();

    @Override
    public void handle(RoutingContext event) {
        try {
            String path = event.normalisedPath().substring(event.mountPoint().length());
            if (path.isEmpty() || path.equals("/")) {
                sendMainPage(event);
            } else {
                int nsIndex = path.indexOf("/");
                if (nsIndex == -1) {
                    event.response().setStatusCode(404).end();
                    return;
                }
                String namespace = path.substring(0, nsIndex);
                currentExtension.set(namespace);
                URL url = getClass().getClassLoader().getResource("/dev-templates/" + path + ".html");
                if (url != null) {
                    Template template = readTemplate(url);
                    event.response().setStatusCode(200).headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
                    renderTemplate(event, template.instance());
                } else {
                    event.next();
                }
            }
        } catch (IOException e) {
            event.fail(e);
        }
    }

    private static Optional<TemplateLocator.TemplateLocation> locateTemplate(String id) {
        URL url = DevConsole.class.getClassLoader().getResource("/dev-templates/" + id + ".html");
        if (url == null)
            return Optional.empty();
        try {
            String data = readURL(url);
            return Optional.of(new TemplateLocator.TemplateLocation() {

                @Override
                public Reader read() {
                    return new StringReader(data);
                }

                @Override
                public Optional<Variant> getVariant() {
                    return Optional.empty();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    protected void renderTemplate(RoutingContext event, TemplateInstance template) {
        template.renderAsync().handle(new BiFunction<String, Throwable, Object>() {
            @Override
            public Object apply(String s, Throwable throwable) {
                if (throwable != null) {
                    event.fail(throwable);
                } else {
                    event.response().end(s);
                }
                return null;
            }
        });
    }

    public void sendMainPage(RoutingContext event) {
        try {
            Template devTemplate = readTemplate("/dev-templates/index.html");
            Enumeration<URL> extensionDescriptors = getClass().getClassLoader()
                    .getResources("/META-INF/quarkus-extension.yaml");
            List<Map<String, Object>> extensions = new ArrayList<>();
            Yaml yaml = new Yaml();
            while (extensionDescriptors.hasMoreElements()) {
                URL extensionDescriptor = extensionDescriptors.nextElement();
                String desc = readURL(extensionDescriptor);
                Map<String, Object> loaded = yaml.load(desc);
                Map<String, Object> metadata = (Map<String, Object>) loaded.get("metadata");
                Boolean unlisted = (Boolean) metadata.get("unlisted");
                String artifactId = (String) loaded.get("artifact-id");
                String groupId = (String) loaded.get("group-id");
                currentExtension.set(groupId + "." + artifactId);
                URL extensionSimple = getClass().getClassLoader()
                        .getResource("/dev-templates/" + groupId + "." + artifactId + "/" + artifactId + ".html");
                boolean display = (unlisted == null || !unlisted) || extensionSimple != null || metadata.containsKey("guide");
                if (display) {
                    if (extensionSimple != null) {
                        Template template = readTemplate(extensionSimple);
                        Map<String, Object> data = new HashMap<>();
                        data.put("urlbase", groupId + "." + artifactId + "/");
                        String result = template.render(data);
                        loaded.put("_dev", result);
                    }
                    extensions.add(loaded);
                }
            }
            Collections.sort(extensions, (a, b) -> {
                return ((String) a.get("name")).compareTo((String) b.get("name"));
            });
            TemplateInstance instance = devTemplate.data("extensions", extensions);
            renderTemplate(event, instance);
        } catch (IOException e) {
            event.fail(e);

        }
    }

    private Template readTemplate(String path) throws IOException {
        URL url = getClass().getClassLoader().getResource(path);
        return readTemplate(url);
    }

    private Template readTemplate(URL url) throws IOException {
        return engine.parse(readURL(url));
    }

    private static String readURL(URL url) throws IOException {
        try (Scanner scanner = new Scanner(url.openStream(),
                StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            String templateBody = scanner.hasNext() ? scanner.next() : null;
            return templateBody;
        }
    }

}
