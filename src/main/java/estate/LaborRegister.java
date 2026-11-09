package estate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class LaborRegister {
    private final Map<String, String> entries;
    private final Set<String> publicRecords;

    LaborRegister(Map<String, String> entries, Set<String> publicRecords) {
        this.entries = new HashMap<>(entries);
        this.publicRecords = Set.copyOf(publicRecords);
    }

    void correctMillCharge() {
        entries.replace("mill", "The mill household's duplicate charge has been removed.");
    }

    boolean contains(String householdId) {
        return entries.containsKey(householdId);
    }

    Optional<String> inspect(String householdId) {
        if (!publicRecords.contains(householdId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(entries.get(householdId));
    }
}
