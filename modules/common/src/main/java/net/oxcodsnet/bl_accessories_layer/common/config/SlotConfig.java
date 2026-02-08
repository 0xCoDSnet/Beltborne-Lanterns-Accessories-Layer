package net.oxcodsnet.bl_accessories_layer.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.oxcodsnet.beltborne_lanterns.BLMod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and stores the list of allowed accessory slots from
 * {@code config/bl_accessories_layer.json}.
 *
 * <p>Thread-safe: the immutable list is published via a volatile field.
 */
public final class SlotConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "config/bl_accessories_layer.json";
    private static final List<String> DEFAULT_SLOTS = List.of("belt");

    private static volatile List<String> allowedSlots = DEFAULT_SLOTS;

    private SlotConfig() {}

    /**
     * Loads the config from disk. Creates the file with defaults if it does not exist.
     * Should be called once during mod initialization.
     */
    public static void load() {
        Path path = Path.of(CONFIG_FILE);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                JsonObject root = new JsonObject();
                JsonArray arr = new JsonArray();
                for (String s : DEFAULT_SLOTS) arr.add(s);
                root.add("allowed_slots", arr);
                try (Writer writer = Files.newBufferedWriter(path)) {
                    GSON.toJson(root, writer);
                }
                BLMod.LOGGER.info("Created default config: {}", path);
                allowedSlots = DEFAULT_SLOTS;
                return;
            }

            JsonObject root;
            try (Reader reader = Files.newBufferedReader(path)) {
                root = GSON.fromJson(reader, JsonObject.class);
            }

            if (root == null || !root.has("allowed_slots")) {
                BLMod.LOGGER.warn("Config missing 'allowed_slots', using defaults");
                allowedSlots = DEFAULT_SLOTS;
                return;
            }

            JsonArray arr = root.getAsJsonArray("allowed_slots");
            List<String> slots = new ArrayList<>(arr.size());
            for (JsonElement el : arr) {
                String slot = el.getAsString().strip();
                if (!slot.isEmpty()) {
                    slots.add(slot);
                }
            }

            if (slots.isEmpty()) {
                BLMod.LOGGER.warn("Config 'allowed_slots' is empty, using defaults");
                allowedSlots = DEFAULT_SLOTS;
            } else {
                allowedSlots = List.copyOf(slots);
                BLMod.LOGGER.info("Loaded allowed slots: {}", allowedSlots);
            }
        } catch (IOException e) {
            BLMod.LOGGER.error("Failed to load config, using defaults", e);
            allowedSlots = DEFAULT_SLOTS;
        }
    }

    /**
     * Returns the immutable list of allowed slot names.
     */
    public static List<String> allowedSlots() {
        return allowedSlots;
    }

    /**
     * Checks whether the given slot name is in the allowed list.
     * Matches both bare names ({@code "belt"}) and namespaced
     * ({@code "accessories:belt"}) against the config entries.
     */
    public static boolean isAllowedSlot(String slotName) {
        if (slotName == null) return false;
        List<String> slots = allowedSlots;
        for (String allowed : slots) {
            if (allowed.equals(slotName)) return true;
            // match "accessories:belt" against config entry "belt"
            if (slotName.endsWith(":" + allowed)) return true;
            // match "belt" against config entry "accessories:belt"
            if (allowed.endsWith(":" + slotName)) return true;
        }
        return false;
    }
}
