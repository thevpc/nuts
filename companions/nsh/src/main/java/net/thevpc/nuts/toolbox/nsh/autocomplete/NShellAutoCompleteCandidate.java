package net.thevpc.nuts.toolbox.nsh.autocomplete;

public class NShellAutoCompleteCandidate {
    private String value;
    private String display;

    public NShellAutoCompleteCandidate(String value) {
        this.value = value;
        this.display = value;
    }

    public NShellAutoCompleteCandidate(String value, String display) {
        this.value = value;
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public String getValue() {
        return value;
    }
}
