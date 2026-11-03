package com.raufixbaba.voicechanger.client.theme;

import com.raufixbaba.voicechanger.RaufixVoiceChanger;
import com.raufixbaba.voicechanger.client.i18n.ModLanguage;
import com.raufixbaba.voicechanger.client.i18n.RaufixI18n;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Persists UI theme, language, picker behavior, and favorite voices across sessions.
 */
public final class UiConfigStore {
    private static final String FILE_NAME = "ui-settings.properties";

    private final Path file;
    private boolean closePickerOnSelect = false;
    private final Set<String> favoriteVoices = new LinkedHashSet<>();

    public UiConfigStore(Path modConfigDir) {
        this.file = modConfigDir.resolve(FILE_NAME);
    }

    public synchronized void load() {
        if (!Files.isRegularFile(this.file)) {
            RaufixI18n.setLanguage(ModLanguage.TR);
            RaufixTheme.setPalette(ThemePalette.CHROMA_RGB);
            this.favoriteVoices.clear();
            this.favoriteVoices.add("woman");
            this.favoriteVoices.add("soft_girl");
            this.favoriteVoices.add("man");
            this.favoriteVoices.add("trap_autotune");
            save();
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(this.file)) {
            props.load(in);
            RaufixI18n.setLanguage(ModLanguage.fromCode(props.getProperty("language", "tr_tr")));
            RaufixTheme.setPalette(ThemePalette.fromId(props.getProperty("palette", "chroma_rgb")));
            RaufixTheme.setParticlesEnabled(Boolean.parseBoolean(props.getProperty("particles", "true")));
            this.closePickerOnSelect = Boolean.parseBoolean(props.getProperty("closePickerOnSelect", "false"));
            this.favoriteVoices.clear();
            String favs = props.getProperty("favorites", "woman,soft_girl,man,trap_autotune");
            for (String token : favs.split(",")) {
                String trimmed = token.trim();
                if (!trimmed.isEmpty()) {
                    this.favoriteVoices.add(trimmed);
                }
            }
        } catch (IOException exception) {
            RaufixVoiceChanger.LOGGER.warn("Could not read UI settings; using defaults", exception);
        }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(this.file.getParent());
            Properties props = new Properties();
            props.setProperty("language", RaufixI18n.language().code());
            props.setProperty("palette", RaufixTheme.palette().id());
            props.setProperty("particles", Boolean.toString(RaufixTheme.particlesEnabled()));
            props.setProperty("closePickerOnSelect", Boolean.toString(this.closePickerOnSelect));
            props.setProperty("favorites", String.join(",", this.favoriteVoices));
            try (OutputStream out = Files.newOutputStream(this.file)) {
                props.store(out, "Raufix Voice Changer V1.0 BETA UI Settings — by RaufixBaba");
            }
        } catch (IOException exception) {
            RaufixVoiceChanger.LOGGER.warn("Could not persist UI settings", exception);
        }
    }

    public synchronized void setLanguage(ModLanguage language) {
        RaufixI18n.setLanguage(language);
        save();
    }

    public synchronized void setPalette(ThemePalette palette) {
        RaufixTheme.setPalette(palette);
        save();
    }

    public synchronized boolean closePickerOnSelect() {
        return this.closePickerOnSelect;
    }

    public synchronized void setClosePickerOnSelect(boolean close) {
        this.closePickerOnSelect = close;
        save();
    }

    public synchronized boolean isFavorite(String voiceId) {
        return this.favoriteVoices.contains(voiceId);
    }

    public synchronized void toggleFavorite(String voiceId) {
        if (voiceId == null || voiceId.isBlank()) {
            return;
        }
        if (!this.favoriteVoices.remove(voiceId)) {
            this.favoriteVoices.add(voiceId);
        }
        save();
    }

    public synchronized Set<String> favorites() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(this.favoriteVoices));
    }
}
