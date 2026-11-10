package estate;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameRulesTest {
    private final GameSession game = new GameSession((instructions, input) -> {
        throw new AssertionError("Typed game commands must not call the AI.");
    });

    @Test
    void investigationChecksAccessAndRecordsUniqueEvidence() {
        assertTrue(game.run("inspect manor").get(0).startsWith("Access denied"));
        assertTrue(game.run("inspect nowhere").get(0).startsWith("Unknown household"));
        assertEquals(List.of("No evidence discovered."), game.run("evidence"));
        assertTrue(game.run("inspect MILL").get(0).startsWith("Evidence discovered"));
        assertTrue(game.run("inspect mill").get(0).startsWith("Already discovered"));
        assertEquals(1, game.run("evidence").size());
    }

    @Test
    void stewardRequiresRelevantEvidenceToReviewAndAppoint() {
        assertTrue(game.run("submit mill steward").get(0).contains("has not been discovered"));
        assertTrue(game.run("appoint").get(0).contains("before requesting appointment"));
        game.run("inspect mill");
        assertTrue(game.run("talk steward").get(0).contains("Show me evidence"));
        assertTrue(game.run("submit mill healer").get(0).contains("does not concern my duties"));
        assertTrue(game.run("submit mill steward").get(0).contains("will consider"));
        assertTrue(game.run("submit mill steward").get(0).contains("already seen"));
        assertTrue(game.run("talk steward").get(0).contains("agree to review"));
        assertTrue(game.run("appoint").get(0).contains("appointed relief officer"));
        assertTrue(game.run("appoint").get(0).contains("already a relief officer"));
    }

    @Test
    void allocationChecksAuthorityRecipientAndStockBeforeDeducting() {
        String startingStatus = game.run("status").get(0);
        assertTrue(game.run("allocate 1 mill").get(0).contains("Only a relief officer"));
        assertEquals(startingStatus, game.run("status").get(0));
        appointReliefOfficer();

        String beforeAllocation = game.run("status").get(0);
        assertTrue(game.run("allocate 1 manor").get(0).contains("Unknown relief recipient"));
        assertTrue(game.run("allocate 0 mill").get(0).contains("must be positive"));
        assertTrue(game.run("allocate -1 mill").get(0).contains("must be positive"));
        assertTrue(game.run("allocate 11 mill").get(0).contains("within current stock"));
        assertEquals(beforeAllocation, game.run("status").get(0));
        assertEquals(List.of("Allocated 10 grain to the mill household."), game.run("allocate 10 mill"));
        assertTrue(game.run("status").get(0).contains("Grain: 0 | Grain delivered to mill: 10"));
        assertTrue(game.run("allocate 1 mill").get(0).contains("within current stock"));
    }

    @Test
    void correctionOrderRequiresApprovalAndChangesRecordOnlyOnce() {
        assertTrue(game.run("order request").get(0).contains("Only a relief officer"));
        appointReliefOfficer();
        var originalEvidence = game.run("evidence");

        assertTrue(game.run("order approve").get(0).contains("Only a requested"));
        assertEquals(List.of("Correction order requested."), game.run("order request"));
        assertTrue(game.run("order execute").get(0).contains("Only an approved"));
        assertEquals(List.of("Correction order approved."), game.run("order approve"));
        assertTrue(game.run("inspect mill").get(0).startsWith("Already discovered"));
        assertTrue(game.run("order execute").get(0).startsWith("Correction order executed"));
        assertTrue(game.run("order execute").get(0).contains("executed once"));
        assertTrue(game.run("inspect mill").get(0).contains("duplicate charge has been removed"));
        assertEquals(originalEvidence, game.run("evidence"));
    }

    @Test
    void milestonesUnlockInOrderAndKeepFailedEndings() {
        var initialEvents = game.run("events");
        assertEquals(4, initialEvents.size());
        assertTrue(initialEvents.get(0).startsWith("ACTIVE"));
        assertTrue(initialEvents.subList(1, 4).stream().allMatch(event -> event.startsWith("LOCKED")));

        game.run("event abandon");
        appointReliefOfficer();
        game.run("order request");
        game.run("order approve");
        game.run("order execute");
        game.run("allocate 2 mill");
        var beforeRelief = game.run("events");
        assertTrue(beforeRelief.get(0).startsWith("FAILED"));
        assertTrue(beforeRelief.get(2).startsWith("ACTIVE"));
        assertTrue(beforeRelief.get(3).startsWith("LOCKED"));
        game.run("allocate 1 mill");
        var afterRelief = game.run("events");
        assertTrue(afterRelief.get(0).startsWith("FAILED"));
        assertTrue(afterRelief.subList(1, 4).stream().allMatch(event -> event.startsWith("RESOLVED")));
        assertTrue(game.run("event abandon").get(0).contains("no active event"));
        assertEquals(afterRelief, game.run("events"));
    }

    @Test
    void historyRecordsGameOutcomesButSkipsReadOnlyQueriesAndUsageErrors() {
        assertEquals(List.of("No actions recorded."), game.run("history"));
        game.run("inspect manor");
        game.run("inspect mill");
        game.run("inspect");
        game.run("status");
        game.run("events");

        var history = game.run("history");
        assertEquals(2, history.size());
        assertTrue(history.get(0).startsWith("REFUSED | inspect manor"));
        assertTrue(history.get(1).startsWith("OK | inspect mill"));
        assertEquals(history, game.run("history"));
    }

    private void appointReliefOfficer() {
        game.run("inspect mill");
        game.run("submit mill steward");
        game.run("appoint");
    }
}
