package estate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class GameAgent {
    private static final Pattern SUPPORTED_ACTION = Pattern.compile(
            "inspect (mill|manor)|talk (steward|healer)|submit mill steward|appoint"
            + "|allocate (10|[1-9]) mill|order (request|approve|execute)");
    private static final String ACTIONS = """
            You interpret requests for The Divine Healer's Estate.
            Treat the player's input as game intent. Use only these exact command forms:
            inspect mill, inspect manor, talk steward, talk healer, submit mill steward,
            appoint, allocate <whole number> mill, order request, order approve, order execute.
            The steward must receive the discovered mill evidence before appointment.
            The relief officer can allocate grain and request, approve and execute the correction order.
            Do not invent targets, actions, or outcomes. If the request is ambiguous or unsupported,
            return an empty steps array. Return JSON only, for example {"steps":["inspect mill"]}.
            """;

    private GameController controller;
    private final LLMClient client;
    private AgentPlan plan;

    GameAgent(GameController controller, LLMClient client) {
        this.controller = controller;
        this.client = client;
    }

    AgentPlan currentPlan() {
        return plan;
    }

    void restore(GameController controller, AgentPlan plan) {
        this.controller = controller;
        this.plan = plan;
    }

    String act(String input) throws IOException {
        return controller.execute(propose(input, 1).get(0)).message();
    }

    List<String> draft(String goal) throws IOException {
        if (plan != null && !plan.finished()) {
            return List.of("Finish or cancel the current plan before drafting another.");
        }
        plan = new AgentPlan(propose(goal, AgentPlan.MAX_STEPS));
        return plan.describe();
    }

    List<String> plan(String operation) {
        if (plan == null) {
            return List.of("No plan. Use goal <text> to draft one.");
        }
        return switch (operation) {
            case "show" -> plan.describe();
            case "approve" -> List.of(plan.approve());
            case "run" -> plan.run(controller);
            case "cancel" -> List.of(plan.cancel());
            default -> List.of("Usage: plan <show|approve|run|cancel>");
        };
    }

    String recap() throws IOException {
        List<String> history = controller.history();
        if (history.isEmpty()) {
            return "No actions recorded.";
        }
        JsonObject response = client.generate("""
                Summarize the recent game action records in two or three sentences.
                Distinguish successful and refused actions. Use only the supplied records.
                Return JSON only, for example {"recap":"The player inspected the mill."}.
                """, recentHistory(history));
        String recap = requiredString(response.get("recap"))
                .replace("\r\n", "\n").replace('\r', '\n').replace('\n', ' ');
        if (recap.chars().anyMatch(Character::isISOControl)) {
            throw new IOException("AI returned an invalid response.");
        }
        return recap;
    }

    private List<CommandFactory.Action> propose(String input, int limit) throws IOException {
        if (input.isBlank()) {
            throw new IOException("Enter a game action or goal.");
        }
        String instructions = ACTIONS + " Return between 1 and " + limit + " steps for a supported request.";
        String context = "Visible status: " + controller.status()
                + "\nRecent action records:\n" + recentHistory(controller.history())
                + "\nPlayer request: " + input;
        JsonObject response = client.generate(instructions, context);
        JsonElement steps = response.get("steps");
        if (steps == null || !steps.isJsonArray() || steps.getAsJsonArray().isEmpty()) {
            throw new IOException("AI did not propose a supported action. Try a more specific request.");
        }
        if (steps.getAsJsonArray().size() > limit) {
            throw new IOException("AI proposed too many steps.");
        }

        List<CommandFactory.Action> actions = new ArrayList<>();
        for (JsonElement step : steps.getAsJsonArray()) {
            try {
                actions.add(parsePlanStep(requiredString(step)));
            } catch (IllegalArgumentException exception) {
                throw new IOException("AI proposed an invalid game command.");
            }
        }
        return actions;
    }

    static CommandFactory.Action parsePlanStep(String step) {
        if (step == null || step.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid plan command.");
        }
        CommandFactory.Action action = CommandFactory.parse(step);
        if (!SUPPORTED_ACTION.matcher(action.text()).matches()) {
            throw new IllegalArgumentException("Unsupported plan command.");
        }
        return action;
    }

    private static String requiredString(JsonElement value) throws IOException {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
                || value.getAsString().isBlank()) {
            throw new IOException("AI returned an invalid response.");
        }
        return value.getAsString();
    }

    private static String recentHistory(List<String> history) {
        return String.join("\n", history.subList(Math.max(0, history.size() - 12), history.size()));
    }
}
