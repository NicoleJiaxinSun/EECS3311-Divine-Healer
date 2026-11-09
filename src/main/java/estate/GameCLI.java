package estate;

import java.util.Locale;
import java.util.Scanner;

public final class GameCLI {
    private static final String HELP = "Commands: inspect <household>, evidence, help, quit";

    public static void main(String[] args) {
        GameController controller = new GameController();
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
                    case "inspect" -> {
                        ActionResult result = controller.inspect(argument);
                        System.out.println(result.message());
                    }
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
}
