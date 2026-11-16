package estate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class LocalConfigurationTest {
    @TempDir
    Path directory;

    @Test
    void environmentKeyTakesPrecedenceOverLocalConfiguration() throws IOException {
        Path file = directory.resolve("config.local.json");
        Files.writeString(file, "invalid JSON");
        assertEquals("environment-test-key", LocalConfiguration.apiKey("environment-test-key", file));
    }

    @Test
    void readsKeyFromTheLocalFileWhenEnvironmentIsAbsent() throws IOException {
        Path file = directory.resolve("config.local.json");
        Files.writeString(file, "{\"DEEPSEEK_API_KEY\":\"local-test-key\"}");
        assertEquals("local-test-key", LocalConfiguration.apiKey(null, file));
        assertEquals("local-test-key", LocalConfiguration.apiKey(" ", file));
    }

    @Test
    void rejectsMissingPlaceholderAndMalformedConfigurationWithoutEchoingValues() throws IOException {
        Path file = directory.resolve("config.local.json");
        assertThrows(IOException.class, () -> LocalConfiguration.apiKey(null, file));
        for (String content : new String[] {
                "{\"DEEPSEEK_API_KEY\":\"YOUR_LOCAL_API_KEY_HERE\"}",
                "{\"DEEPSEEK_API_KEY\":123}",
                "{\"DEEPSEEK_API_KEY\":\"\"}",
                "null",
                "{\"DEEPSEEK_API_KEY\": secret-test-value"
        }) {
            Files.writeString(file, content);
            var error = assertThrows(IOException.class, () -> LocalConfiguration.apiKey(null, file));
            assertFalse(error.getMessage().contains("secret-test-value"));
        }
    }
}
