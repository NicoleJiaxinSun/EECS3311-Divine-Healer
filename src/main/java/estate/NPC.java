package estate;

import java.util.HashSet;
import java.util.Set;

final class NPC {
    private final String name;
    private final NegotiationStrategy strategy;
    private final Set<String> relevantEvidence;
    private final Set<String> knownEvidence = new HashSet<>();

    NPC(String name, NegotiationStrategy strategy, Set<String> relevantEvidence) {
        this.name = name;
        this.strategy = strategy;
        this.relevantEvidence = Set.copyOf(relevantEvidence);
    }

    ActionResult negotiate() {
        return strategy.evaluate(Set.copyOf(knownEvidence));
    }

    boolean knows(String evidenceId) {
        return knownEvidence.contains(evidenceId);
    }

    ActionResult learn(String evidenceId) {
        if (!relevantEvidence.contains(evidenceId)) {
            return new ActionResult(false, name + ": This evidence does not concern my duties.");
        }
        if (!knownEvidence.add(evidenceId)) {
            return new ActionResult(true, name + ": I have already seen this evidence.");
        }
        return new ActionResult(true, name + ": I will consider this evidence.");
    }
}
