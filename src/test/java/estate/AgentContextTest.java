package estate;

import com.google.gson.JsonParser;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentContextTest {
    @Test
    void longHistoryKeepsDiscoveredEvidenceAndCurrentEventsVisibleToThePlanner() {
        AtomicReference<String> context = new AtomicReference<>();
        GameSession game = new GameSession((instructions, input) -> {
            context.set(input);
            return JsonParser.parseString("{\"steps\":[\"appoint\"]}").getAsJsonObject();
        });
        game.run("inspect mill");
        game.run("submit mill steward");
        for (int index = 0; index < 13; index++) {
            game.run("talk healer");
        }
        var history = game.run("history");
        var events = game.run("events");
        game.run("goal Continue helping the mill");

        String prompt = context.get();
        assertTrue(prompt.contains("The mill household was charged for the same shift twice."));
        for (String event : events) {
            assertTrue(prompt.contains(event));
        }
        String recent = prompt.substring(prompt.indexOf("Recent action records:"));
        assertFalse(recent.contains("OK | inspect mill |"));
        assertFalse(recent.contains("OK | submit mill steward |"));
        assertEquals(history, game.run("history"));
        assertTrue(game.run("status").get(0).contains("Rank: visitor"));
        assertEquals("Plan: DRAFT", game.run("plan show").get(0));
    }
}
