package estate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeepSeekAdapterTest {
    private record Request(String method, String authorization, String contentType, String body) {}
    private record Response(int status, String body) {}

    private final AtomicReference<Request> received = new AtomicReference<>();
    private final AtomicReference<Response> reply = new AtomicReference<>();
    private HttpServer server;
    private URI endpoint;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", exchange -> {
            try (exchange) {
                received.set(new Request(exchange.getRequestMethod(),
                        exchange.getRequestHeaders().getFirst("Authorization"),
                        exchange.getRequestHeaders().getFirst("Content-Type"),
                        new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
                Response response = reply.get();
                byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(response.status(), bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        });
        server.start();
        endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/chat/completions");
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void sendsChatCompletionRequestAndParsesStructuredMessage() throws IOException {
        reply.set(completion("stop", "{\"steps\":[\"inspect mill\"]}"));
        JsonObject response = adapter().generate("Return JSON.", "调查磨坊的记录");
        assertEquals("inspect mill", response.getAsJsonArray("steps").get(0).getAsString());

        Request request = received.get();
        assertEquals("POST", request.method());
        assertEquals("Bearer test-key", request.authorization());
        assertEquals("application/json", request.contentType());
        var payload = JsonParser.parseString(request.body()).getAsJsonObject();
        assertEquals("deepseek-flash", payload.get("model").getAsString());
        assertEquals("disabled", payload.getAsJsonObject("thinking").get("type").getAsString());
        assertEquals("json_object", payload.getAsJsonObject("response_format").get("type").getAsString());
        var messages = payload.getAsJsonArray("messages");
        assertEquals("system", messages.get(0).getAsJsonObject().get("role").getAsString());
        assertEquals("Return JSON.", messages.get(0).getAsJsonObject().get("content").getAsString());
        assertEquals("user", messages.get(1).getAsJsonObject().get("role").getAsString());
        assertEquals("调查磨坊的记录", messages.get(1).getAsJsonObject().get("content").getAsString());
    }

    @Test
    void missingKeyStopsBeforeSendingARequest() {
        var adapter = new DeepSeekAdapter(" ", endpoint);
        var error = assertThrows(IOException.class, () -> adapter.generate("Return JSON.", "inspect mill"));
        assertTrue(error.getMessage().contains("DEEPSEEK_API_KEY"));
        assertNull(received.get());
    }

    @Test
    void providerErrorsAreReportedWithoutEchoingTheResponseBody() {
        for (int status : List.of(401, 429, 503)) {
            reply.set(new Response(status, "{\"error\":\"private provider details\"}"));
            var error = assertThrows(IOException.class, () -> adapter().generate("Return JSON.", "inspect mill"));
            assertEquals("DeepSeek returned HTTP " + status + ".", error.getMessage());
        }
    }

    @Test
    void incompleteCompletionIsRejectedEvenIfItsContentLooksValid() {
        reply.set(completion("length", "{\"steps\":[\"inspect mill\"]}"));
        var error = assertThrows(IOException.class, () -> adapter().generate("Return JSON.", "inspect mill"));
        assertEquals("DeepSeek did not finish the response.", error.getMessage());
    }

    @Test
    void invalidEnvelopeOrMessageContentIsRejected() {
        for (Response invalid : List.of(
                new Response(200, "{\"choices\":[]}"),
                new Response(200, "{choices:[]}"),
                completion("stop", "{steps:[\"inspect mill\"]}"),
                completion("stop", "[]"),
                completion("stop", "null"),
                completion("stop", "   "),
                completion("stop", 123))) {
            reply.set(invalid);
            assertThrows(IOException.class, () -> adapter().generate("Return JSON.", "inspect mill"), invalid.body());
        }
    }

    private DeepSeekAdapter adapter() {
        return new DeepSeekAdapter("test-key", endpoint);
    }

    private static Response completion(String finishReason, Object content) {
        String body = new Gson().toJson(Map.of("choices", List.of(Map.of(
                "finish_reason", finishReason, "message", Map.of("content", content)))));
        return new Response(200, body);
    }
}
