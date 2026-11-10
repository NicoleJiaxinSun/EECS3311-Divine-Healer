package estate;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class GamePersistenceTest {
    @TempDir
    Path directory;

    @Test
    void restoresGameStateInAnotherSession() {
        Path saveFile = directory.resolve("my game.json");
        GameSession original = offlineSession();
        for (String command : List.of("event abandon", "inspect mill", "submit mill steward",
                "appoint", "allocate 3 mill", "order request", "order approve", "order execute")) {
            original.run(command);
        }
        assertTrue(original.run("save " + saveFile).get(0).startsWith("Game saved:"));

        GameSession restored = offlineSession();
        assertTrue(restored.run("load " + saveFile).get(0).startsWith("Game loaded:"));
        for (String command : List.of("status", "events", "evidence", "history")) {
            assertEquals(original.run(command), restored.run(command), command);
        }
        assertTrue(restored.run("inspect mill").get(0).contains("duplicate charge has been removed"));
    }

    @Test
    void restoringAnApprovedPlanRequiresApprovalAndResumesAtTheSavedStep() {
        Path saveFile = directory.resolve("plan.json");
        GameSession original = new GameSession((instructions, input) ->
                JsonParser.parseString("""
                        {"steps":["inspect mill","allocate 3 mill"]}
                        """).getAsJsonObject());
        original.run("goal help the mill");
        assertEquals(List.of("No actions recorded."), original.run("history"));
        assertTrue(original.run("plan run").get(0).startsWith("Approve"));
        original.run("plan approve");
        assertTrue(original.run("plan run").get(2).startsWith("Plan paused"));
        original.run("submit mill steward");
        original.run("appoint");
        original.run("plan approve");
        assertTrue(original.run("save " + saveFile).get(0).startsWith("Game saved:"));

        GameSession restored = offlineSession();
        assertTrue(restored.run("load " + saveFile).get(0).startsWith("Game loaded:"));
        assertEquals(List.of("Plan: PAUSED", "1. inspect mill [done]", "2. allocate 3 mill"),
                restored.run("plan show"));
        assertTrue(restored.run("plan run").get(0).startsWith("Approve"));
        restored.run("plan approve");
        assertTrue(restored.run("plan run").contains("Plan completed."));
        assertTrue(restored.run("status").get(0).contains("Grain delivered to mill: 3"));
        assertEquals(1, restored.run("history").stream().filter(line -> line.startsWith("OK | inspect mill")).count());
    }

    @Test
    void failedLoadPreservesTheCurrentSession() throws IOException {
        Path saveFile = directory.resolve("invalid.json");
        GameSession game = offlineSession();
        game.run("inspect mill");
        var history = game.run("history");
        var evidence = game.run("evidence");

        for (String invalid : List.of(
                "{broken",
                "{\"version\":2,\"actions\":[]}",
                "{\"version\":1,\"actions\":[\"inspect manor\",\"unknown action\"]}")) {
            Files.writeString(saveFile, invalid);
            assertTrue(game.run("load " + saveFile).get(0).startsWith("Load failed:"), invalid);
            assertEquals(history, game.run("history"));
            assertEquals(evidence, game.run("evidence"));
        }
    }

    private static GameSession offlineSession() {
        return new GameSession((instructions, input) -> {
            throw new AssertionError("Loading and executing saved steps must not call the AI.");
        });
    }
}
