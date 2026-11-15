package estate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

final class JsonGameStateRepository {
    private static final int VERSION = 1;
    private final Gson gson = new GsonBuilder().setStrictness(Strictness.STRICT)
            .setPrettyPrinting().create();

    private record SaveFile(Integer version, List<String> actions, AgentPlan.Snapshot plan) {}
    record LoadedGame(GameController controller, AgentPlan plan) {}

    void save(Path file, GameController controller, AgentPlan plan) throws IOException {
        Path target = file.toAbsolutePath();
        if (target.getFileName() == null || Files.isDirectory(target)) {
            throw new IOException("Choose a file path.");
        }
        Path temporary = Files.createTempFile(target.getParent(), ".estate-", ".tmp");
        try {
            SaveFile save = new SaveFile(VERSION, controller.recordedActions(),
                    plan == null ? null : plan.snapshot());
            try (var writer = Files.newBufferedWriter(temporary)) {
                gson.toJson(save, writer);
            }
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (JsonParseException exception) {
            throw new IOException("Could not write the game data.", exception);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    LoadedGame load(Path file) throws IOException {
        try (var reader = Files.newBufferedReader(file)) {
            JsonObject data = gson.fromJson(reader, JsonObject.class);
            validateTypes(data);
            SaveFile save = gson.fromJson(data, SaveFile.class);
            if (save == null || save.version() == null || save.version() != VERSION
                    || save.actions() == null || save.actions().stream().anyMatch(action -> action == null)) {
                throw new IOException("Invalid or unsupported save file.");
            }

            GameController restored = new GameController();
            for (String action : save.actions()) {
                if (action.equals("event abandon")) {
                    restored.abandonEvent();
                } else {
                    restored.execute(CommandFactory.parse(action));
                }
            }
            AgentPlan plan = save.plan() == null ? null : AgentPlan.restore(save.plan());
            return new LoadedGame(restored, plan);
        } catch (JsonParseException | IllegalArgumentException exception) {
            throw new IOException("Invalid or unsupported save file.", exception);
        }
    }
    private static void validateTypes(JsonObject data) throws IOException {
        if (data == null || !integer(data.get("version")) || !strings(data.get("actions"))) {
            throw new IOException("Invalid save field types.");
        }
        JsonElement plan = data.get("plan");
        if (plan == null || plan.isJsonNull()) {
            return;
        }
        if (!plan.isJsonObject()) {
            throw new IOException("Invalid saved plan.");
        }
        JsonObject snapshot = plan.getAsJsonObject();
        if (!strings(snapshot.get("steps")) || !integer(snapshot.get("nextStepIndex"))
                || !string(snapshot.get("status"))) {
            throw new IOException("Invalid saved plan field types.");
        }
    }

    private static boolean integer(JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()
                && value.getAsString().matches("0|[1-9][0-9]*");
    }

    private static boolean strings(JsonElement value) {
        return value != null && value.isJsonArray()
                && value.getAsJsonArray().asList().stream().allMatch(JsonGameStateRepository::string);
    }

    private static boolean string(JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
    }

}
