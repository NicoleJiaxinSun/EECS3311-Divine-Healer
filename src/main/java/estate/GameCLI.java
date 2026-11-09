package estate;

import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public final class GameCLI {
    private static final String HELP = "Commands: inspect <household>, evidence, "
            + "talk <npc>, submit <evidence-id> <npc>, appoint, "
            + "allocate <grain-quantity> <recipient>, order <request|approve|execute>, "
            + "events, event abandon, history, status, act <text>, goal <text>, "
            + "plan <show|approve|run|cancel>, recap, help, quit";

    public static void main(String[] args) {
        GameController controller = new GameController();
        GameAgent agent = new GameAgent(controller, new DeepSeekAdapter(System.getenv("DEEPSEEK_API_KEY")));
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("The Divine Healer's Estate");
            System.out.println(HELP);
            while (input.hasNextLine()) {
                String line = input.nextLine().strip();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+", 2);
                String argument = parts.length == 2 ? parts[1] : "";
                switch (parts[0].toLowerCase(Locale.ROOT)) {
                    case "inspect", "talk", "submit", "appoint", "allocate", "order" ->
                        runAction(controller, line);
                    case "act", "goal", "recap" ->
                        runAI(agent, parts[0].toLowerCase(Locale.ROOT), argument);
                    case "plan" -> agent.plan(argument.toLowerCase(Locale.ROOT)).forEach(System.out::println);
                    case "events" -> controller.events().forEach(System.out::println);
                    case "event" -> {
                        if (argument.equalsIgnoreCase("abandon")) {
                            System.out.println(controller.abandonEvent().message());
                        } else {
                            System.out.println("Usage: event abandon");
                        }
                    }
                    case "history" -> {
                        var history = controller.history();
                        if (history.isEmpty()) {
                            System.out.println("No actions recorded.");
                        }
                        history.forEach(System.out::println);
                    }
                    case "status" -> System.out.println(controller.status());
                    case "evidence" -> {
                        var evidence = controller.evidence();
                        if (evidence.isEmpty()) {
                            System.out.println("No evidence discovered.");
                        }
                        evidence.forEach(System.out::println);
                    }
                    case "help" -> System.out.println(HELP);
                    case "quit" -> {
                        return;
                    }
                    default -> System.out.println("Unknown command. Type help for available commands.");
                }
            }
        }
    }

    private static void runAction(GameController controller, String line) {
        try {
            System.out.println(controller.execute(CommandFactory.parse(line)).message());
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void runAI(GameAgent agent, String command, String argument) {
        try {
            switch (command) {
                case "act" -> System.out.println(agent.act(argument));
                case "goal" -> agent.draft(argument).forEach(System.out::println);
                case "recap" -> System.out.println(agent.recap());
            }
        } catch (IOException exception) {
            System.out.println("AI unavailable: " + exception.getMessage());
        }
    }
}
