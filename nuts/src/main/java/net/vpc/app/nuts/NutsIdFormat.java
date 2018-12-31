package net.vpc.app.nuts;

public interface NutsIdFormat {
    boolean isOmitNamespace();

    NutsIdFormat setOmitNamespace(boolean omitNamespace);

    boolean isOmitGroup();

    NutsIdFormat setOmitGroup(boolean omitGroup);

    boolean isOmitImportedGroup();

    NutsIdFormat setOmitImportedGroup(boolean omitImportedGroup);

    boolean isOmitEnv();

    NutsIdFormat setOmitEnv(boolean omitEnv);

    boolean isOmitFace();

    NutsIdFormat setOmitFace(boolean omitFace);

    boolean isHighlightImportedGroup();

    NutsIdFormat setHighlightImportedGroup(boolean highlightImportedGroup);

    boolean isHighlightScope();

    NutsIdFormat setHighlightScope(boolean highlightScope);

    boolean isHighlightOptional();

    NutsIdFormat setHighlightOptional(boolean highlightOptional);

    String format(NutsId id);
}
