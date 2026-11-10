package estate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

final class DeepSeekAdapter implements LLMClient {
    private static final URI ENDPOINT = URI.create("https://api.deepseek.com/chat/completions");
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Gson gson = new GsonBuilder().setStrictness(Strictness.STRICT).create();
    private final URI endpoint;
    private final String apiKey;

    DeepSeekAdapter(String apiKey) {
        this(apiKey, ENDPOINT);
    }

    DeepSeekAdapter(String apiKey, URI endpoint) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
    }

    @Override
    public JsonObject generate(String instructions, String input) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("Set DEEPSEEK_API_KEY to use AI commands.");
        }

        String body = gson.toJson(Map.of(
                "model", "deepseek-flash",
                "thinking", Map.of("type", "disabled"),
                "max_tokens", 600,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", instructions),
                        Map.of("role", "user", "content", input))));
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("DeepSeek request interrupted.", exception);
        } catch (IOException exception) {
            throw new IOException("Could not reach DeepSeek.", exception);
        }
        if (response.statusCode() != 200) {
            throw new IOException("DeepSeek returned HTTP " + response.statusCode() + ".");
        }

        try {
            var choice = gson.fromJson(response.body(), JsonObject.class)
                    .getAsJsonArray("choices").get(0).getAsJsonObject();
            if (!"stop".equals(choice.get("finish_reason").getAsString())) {
                throw new IOException("DeepSeek did not finish the response.");
            }
            var content = choice.getAsJsonObject("message").getAsJsonPrimitive("content");
            if (!content.isString() || content.getAsString().isBlank()) {
                throw new IOException("DeepSeek returned no usable message content.");
            }
            JsonObject result = gson.fromJson(content.getAsString(), JsonObject.class);
            if (result == null) {
                throw new IOException("DeepSeek returned an invalid JSON response.");
            }
            return result;
        } catch (RuntimeException exception) {
            throw new IOException("DeepSeek returned an invalid JSON response.", exception);
        }
    }
}
