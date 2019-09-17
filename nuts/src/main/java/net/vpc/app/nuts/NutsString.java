package net.vpc.app.nuts;

public class NutsString {
    private String value;

    public NutsString(String value) {
        this.value = value == null ? "" : value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
