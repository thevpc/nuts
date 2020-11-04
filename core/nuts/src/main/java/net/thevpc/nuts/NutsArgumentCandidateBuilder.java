package net.thevpc.nuts;

import java.io.Serializable;

public interface NutsArgumentCandidateBuilder extends Serializable {

    /**
     * argument value
     *
     * @return argument value
     */
    String getValue();

    /**
     * human display
     *
     * @return human display
     */
    String getDisplay();

    NutsArgumentCandidateBuilder setValue(String value);

    NutsArgumentCandidateBuilder setDisplay(String value);

    NutsArgumentCandidateBuilder setCandidate(NutsArgumentCandidate value);

    NutsArgumentCandidate build();

}
