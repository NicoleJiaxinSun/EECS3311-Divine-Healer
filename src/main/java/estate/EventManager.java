package estate;

import java.util.List;

final class EventManager implements GameEventListener {
    private final List<PoliticalEvent> sequence = List.of(
            new PoliticalEvent("Present the mill evidence", state -> state.npc("steward").knows("mill")),
            new PoliticalEvent("Gain the relief appointment", GameState::isReliefOfficer),
            new PoliticalEvent("Deliver at least 3 grain to the mill",
                    state -> state.inventory().millGrainDelivered() >= 3),
            new PoliticalEvent("Correct the duplicate mill charge",
                    state -> state.correctionOrder() == IssueOrderCommand.Status.EXECUTED));
    private int activeIndex;

    EventManager() {
        sequence.get(0).activate();
    }

    @Override
    public void onGameEvent(GameState state, String action, ActionResult result) {
        if (!result.success()) {
            return;
        }
        while (activeIndex < sequence.size()) {
            PoliticalEvent event = sequence.get(activeIndex);
            event.handleAction(state);
            if (!event.finished()) {
                return;
            }
            advance();
        }
    }

    ActionResult abandonActive() {
        if (activeIndex == sequence.size()) {
            return new ActionResult(false, "There is no active event to abandon.");
        }
        PoliticalEvent event = sequence.get(activeIndex);
        event.fail();
        advance();
        return new ActionResult(true, "Event abandoned: " + event.title());
    }

    List<String> events() {
        return sequence.stream().map(PoliticalEvent::describe).toList();
    }

    private void advance() {
        activeIndex++;
        if (activeIndex < sequence.size()) {
            sequence.get(activeIndex).activate();
        }
    }
}
