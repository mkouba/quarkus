package io.quarkus.devconsole.spi;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class FlashScopeUtil {
    private final static String FLASH_COOKIE_NAME = "_flash";
    private final static String FLASH_CONTEXT_DATA_NAME = "flash";

    public static void setFlash(RoutingContext event, JsonObject data) {
        setFlash(event, data.encode());
    }

    public static void setFlash(RoutingContext event, JsonArray data) {
        setFlash(event, data.encode());
    }

    public static void setFlash(RoutingContext event, String data) {
        event.response().addCookie(
                Cookie.cookie(FLASH_COOKIE_NAME, Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8))));
    }

    public static Object getFlash(RoutingContext event) {
        return event.data().get(FLASH_CONTEXT_DATA_NAME);
    }

    public static void handleFlashCookie(RoutingContext event) {
        Cookie cookie = event.request().getCookie(FLASH_COOKIE_NAME);
        event.response().removeCookie(FLASH_COOKIE_NAME);
        if (cookie != null) {
            String value = cookie.getValue();
            String decodedData = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
            Object data;
            // this is not really valid JSON since we don't quote the string and don't support numbers or booleans or null
            if (decodedData.startsWith("{"))
                data = new JsonObject(decodedData);
            else if (decodedData.startsWith("["))
                data = new JsonArray(decodedData);
            else
                data = decodedData;
            event.data().put(FLASH_CONTEXT_DATA_NAME, data);
        }
    }

    public static void setFlashMessage(RoutingContext event, String message, String messageClass) {
        setFlash(event, new JsonObject().put("message", new JsonObject().put("text", message).put("class", messageClass)));
    }
}
