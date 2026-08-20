package net.thevpc.nuts.cmdline;

public interface NArgCompleteCandidate {
    static NArgCompleteCandidate of(String value) {
        return new DefaultNArgCompleteCandidate(value);
    }

    static NArgCompleteCandidate of(String value, String display) {
        return new DefaultNArgCompleteCandidate(value, display);
    }

    String value();

    String display();
}
