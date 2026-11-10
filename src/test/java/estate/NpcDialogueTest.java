package estate;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NpcDialogueTest {
    @Test
    void narrationReceivesTheRuleDecisionAndHistoryRecordsEachNegotiationOnce() {
        var decisions = new ArrayList<String>();
        GameSession game = new GameSession((instructions, input) -> {
            decisions.add(input);
            JsonObject response = new JsonObject();
            response.addProperty("dialogue", "The ledger will decide this.");
            return response;
        });

        var refusal = game.run("talk-ai steward");
        assertTrue(refusal.get(0).contains("Show me evidence"));
        assertEquals("AI dialogue: The ledger will decide this.", refusal.get(1));

        game.run("inspect mill");
        game.run("submit mill steward");
        var acceptance = game.run("talk-ai STEWARD");
        assertTrue(acceptance.get(0).contains("agree to review"));
        assertEquals("AI dialogue: The ledger will decide this.", acceptance.get(1));
        assertEquals(2, decisions.size());
        assertTrue(decisions.get(0).contains("Decision: refused"));
        assertTrue(decisions.get(1).contains("Decision: accepted"));

        var negotiations = game.run("history").stream().filter(line -> line.contains("| talk steward |")).toList();
        assertEquals(2, negotiations.size());
        assertTrue(negotiations.get(0).startsWith("REFUSED"));
        assertTrue(negotiations.get(1).startsWith("OK"));
    }

    @Test
    void failedNarrationKeepsTheRuleResultAndHistory() {
        GameSession game = new GameSession((instructions, input) -> {
            throw new IOException("Simulated outage.");
        });

        assertEquals(List.of("Healer: I agree to hear your complaint.", "AI dialogue unavailable: Simulated outage."),
                game.run("talk-ai healer"));
        assertEquals(List.of("OK | talk healer | Healer: I agree to hear your complaint."), game.run("history"));
    }

    @Test
    void malformedNarrationKeepsTheRefusal() {
        GameSession game = new GameSession((instructions, input) -> new JsonObject());
        var result = game.run("talk-ai steward");
        assertTrue(result.get(0).contains("Show me evidence"));
        assertTrue(result.get(1).startsWith("AI dialogue unavailable:"));
        assertEquals(1, game.run("history").size());
    }

    @Test
    void plainTalkAndInvalidNarrationTargetsDoNotCallTheModel() {
        AtomicInteger calls = new AtomicInteger();
        GameSession game = new GameSession((instructions, input) -> {
            calls.incrementAndGet();
            return new JsonObject();
        });

        assertEquals(List.of("Usage: talk-ai <steward|healer>"), game.run("talk-ai stranger"));
        assertEquals(List.of("No actions recorded."), game.run("history"));
        assertEquals(List.of("Healer: I agree to hear your complaint."), game.run("talk healer"));
        assertEquals(0, calls.get());
    }
}
