package estate;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

final class GameSession {
    static final String TITLE = "The Divine Healer's Estate";
    static final String HELP = "Commands: inspect <household>, evidence, "
            + "talk <npc>, submit <evidence-id> <npc>, appoint, "
            + "allocate <grain-quantity> <recipient>, order <request|approve|execute>, "
            + "events, event abandon, history, status, act <text>, goal <text>, "
            + "plan <show|approve|run|cancel>, recap, help";

    private final GameController controller = new GameController();
    private final GameAgent agent;

    GameSession(LLMClient client) {
        agent = new GameAgent(controller, client);
    }

    List<String> run(String input) {
        String line = input.strip();
        if (line.isEmpty()) {
            return List.of();
        }

        String[] parts = line.split("\\s+", 2);
        String argument = parts.length == 2 ? parts[1] : "";
        try {
            return switch (parts[0].toLowerCase(Locale.ROOT)) {
                case "inspect", "talk", "submit", "appoint", "allocate", "order" ->
                    List.of(controller.execute(CommandFactory.parse(line)).message());
                case "act" -> List.of(agent.act(argument));
                case "goal" -> agent.draft(argument);
                case "recap" -> List.of(agent.recap());
                case "plan" -> agent.plan(argument.toLowerCase(Locale.ROOT));
                case "events" -> controller.events();
                case "event" -> argument.equalsIgnoreCase("abandon")
                        ? List.of(controller.abandonEvent().message())
                        : List.of("Usage: event abandon");
                case "history" -> display(controller.history(), "No actions recorded.");
                case "status" -> List.of(controller.status());
                case "evidence" -> display(controller.evidence(), "No evidence discovered.");
                case "help" -> List.of(HELP);
                default -> List.of("Unknown command. Type help for available commands.");
            };
        } catch (IllegalArgumentException exception) {
            return List.of(exception.getMessage());
        } catch (IOException exception) {
            return List.of("AI unavailable: " + exception.getMessage());
        }
    }

    private static List<String> display(List<String> entries, String emptyMessage) {
        return entries.isEmpty() ? List.of(emptyMessage) : entries;
    }
}
