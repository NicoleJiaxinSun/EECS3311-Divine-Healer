package estate;

import java.util.Set;

interface NegotiationStrategy {
    ActionResult evaluate(Set<String> knownEvidence);
}
