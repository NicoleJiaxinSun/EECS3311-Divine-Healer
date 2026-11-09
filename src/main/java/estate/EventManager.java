package estate;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

final class EventManager implements GameEventListener {
    private enum Status {
        LOCKED, ACTIVE, RESOLVED, FAILED
    }

    private enum Milestone {
        EVIDENCE("Present the mill evidence", state -> state.npc("steward").knows("mill")),
        APPOINTMENT("Gain the relief appointment", GameState::isReliefOfficer),
        RELIEF("Deliver at least 3 grain to the mill",
                state -> state.inventory().millGrainDelivered() >= 3),
        CORRECTION("Correct the duplicate mill charge",
                state -> state.correctionOrder() == IssueOrderCommand.Status.EXECUTED);

        private final String title;
        private final Predicate<GameState> achieved;

        Milestone(String title, Predicate<GameState> achieved) {
            this.title = title;
            this.achieved = achieved;
        }
    }

    private final List<Milestone> sequence = List.of(Milestone.values());
    private final Map<Milestone, Status> statuses = new EnumMap<>(Milestone.class);
    private int activeIndex;

    EventManager() {
        sequence.forEach(event -> statuses.put(event, Status.LOCKED));
        statuses.put(sequence.get(0), Status.ACTIVE);
    }

    @Override
    public void onGameEvent(GameState state, String action, ActionResult result) {
        if (!result.success()) {
            return;
        }
        while (activeIndex < sequence.size() && sequence.get(activeIndex).achieved.test(state)) {
            finishActive(Status.RESOLVED);
        }
    }

    ActionResult abandonActive() {
        if (activeIndex == sequence.size()) {
            return new ActionResult(false, "There is no active event to abandon.");
        }
        String title = sequence.get(activeIndex).title;
        finishActive(Status.FAILED);
        return new ActionResult(true, "Event abandoned: " + title);
    }

    List<String> events() {
        return sequence.stream()
                .map(event -> statuses.get(event) + " | " + event.title)
                .toList();
    }

    private void finishActive(Status outcome) {
        statuses.put(sequence.get(activeIndex), outcome);
        activeIndex++;
        if (activeIndex < sequence.size()) {
            statuses.put(sequence.get(activeIndex), Status.ACTIVE);
        }
    }
}
