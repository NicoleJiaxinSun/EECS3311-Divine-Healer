package estate;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class LocalConfiguration {
    static String apiKey(String environmentKey, Path file) throws IOException {
        if (environmentKey != null && !environmentKey.isBlank()) {
            return environmentKey;
        }
        if (!Files.exists(file)) {
            throw new IOException("Set DEEPSEEK_API_KEY or enter a key in config.local.json.");
        }
        try (var reader = Files.newBufferedReader(file)) {
            JsonObject configuration = new GsonBuilder().setStrictness(Strictness.STRICT)
                    .create().fromJson(reader, JsonObject.class);
            var value = configuration == null ? null : configuration.get("DEEPSEEK_API_KEY");
            if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
                    || value.getAsString().isBlank()
                    || value.getAsString().equals("YOUR_LOCAL_API_KEY_HERE")) {
                throw new IOException("Enter a valid DEEPSEEK_API_KEY in config.local.json.");
            }
            return value.getAsString();
        } catch (JsonParseException exception) {
            throw new IOException("Check the JSON format of config.local.json.");
        } catch (IOException exception) {
            // Keep configuration values and parser diagnostics out of user-facing output.
            throw new IOException("Could not load a valid DEEPSEEK_API_KEY from config.local.json.");
        }
    }
}
