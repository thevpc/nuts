package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

public class JShellAutoCompleteCandidate {
    private String value;
    private String display;

    public JShellAutoCompleteCandidate(String value) {
        this.value = value;
        this.display = value;
    }

    public JShellAutoCompleteCandidate(String value, String display) {
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
