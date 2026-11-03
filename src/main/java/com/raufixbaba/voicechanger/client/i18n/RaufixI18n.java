package com.raufixbaba.voicechanger.client.i18n;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Smart localization layer for Raufix Voice Changer.
 *
 * <p>Defaults to Turkish ({@link ModLanguage#TR}) out-of-the-box so Turkish players
 * immediately get the full Turkish UI even when their Minecraft client is left on
 * English (US), while also supporting instant 1-click switching between Turkish,
 * English, and Minecraft's automatic language.</p>
 */
public final class RaufixI18n {
    private static final Map<String, String> TR_MAP = loadBundle("tr_tr");
    private static final Map<String, String> EN_MAP = loadBundle("en_us");
    private static volatile ModLanguage activeLanguage = ModLanguage.TR;

    private RaufixI18n() {
    }

    public static ModLanguage language() {
        return activeLanguage;
    }

    public static ModLanguage languageOverride() {
        return activeLanguage;
    }

    public static void setLanguage(ModLanguage language) {
        activeLanguage = language == null ? ModLanguage.TR : language;
    }

    public static void setLanguageOverride(ModLanguage language) {
        setLanguage(language);
    }

    public static ModLanguage cycleLanguage() {
        ModLanguage next = activeLanguage.next();
        activeLanguage = next;
        return next;
    }

    public static MutableComponent tr(String key, Object... args) {
        ModLanguage lang = activeLanguage;
        if (lang == ModLanguage.AUTO) {
            if (args.length == 0) {
                return Component.translatable(key);
            }
            Object[] resolved = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                resolved[i] = args[i];
            }
            return Component.translatable(key, resolved);
        }
        return Component.literal(string(key, args));
    }

    public static String string(String key, Object... args) {
        ModLanguage lang = activeLanguage;
        Map<String, String> primary = (lang == ModLanguage.EN) ? EN_MAP : TR_MAP;
        Map<String, String> fallback = (lang == ModLanguage.EN) ? TR_MAP : EN_MAP;

        String template = primary.get(key);
        if (template == null) {
            template = fallback.get(key);
        }
        if (template == null) {
            return Component.translatable(key, args).getString();
        }
        if (args == null || args.length == 0) {
            return template;
        }

        Object[] formattedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            formattedArgs[i] = (arg instanceof Component comp) ? comp.getString() : arg;
        }
        try {
            return String.format(Locale.ROOT, template, formattedArgs);
        } catch (IllegalFormatException ignored) {
            return template;
        }
    }

    private static Map<String, String> loadBundle(String code) {
        String resourcePath = "/assets/" + RaufixVoiceChanger.MOD_ID + "/lang/" + code + ".json";
        try (InputStream stream = RaufixI18n.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                RaufixVoiceChanger.LOGGER.warn("Missing language bundle {}", resourcePath);
                return Collections.emptyMap();
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            Map<String, String> map = new HashMap<>(root.size());
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (entry.getValue().isJsonPrimitive()) {
                    map.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            return Collections.unmodifiableMap(map);
        } catch (Exception exception) {
            RaufixVoiceChanger.LOGGER.error("Failed to load language bundle {}", resourcePath, exception);
            return Collections.emptyMap();
        }
    }
}
