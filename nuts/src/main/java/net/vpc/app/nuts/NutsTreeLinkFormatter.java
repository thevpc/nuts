package net.vpc.app.nuts;

public interface NutsTreeLinkFormatter {

    public enum Type {
        FIRST,
        MIDDLE,
        LAST
    }

    String formatMain(Type type);

    String formatChild(Type type);
}
