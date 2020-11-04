package net.thevpc.nuts;

public interface NutsDescriptorFilterManager extends NutsTypedFilters<NutsDescriptorFilter>{
    NutsDescriptorFilter byExpression(String expression);

    NutsDescriptorFilter byPackaging(String... values);
    NutsDescriptorFilter byArch(String... values);

    NutsDescriptorFilter byOsdist(String... values);

    NutsDescriptorFilter byPlatform(String... values);

    NutsDescriptorFilter byExec(Boolean value);

    NutsDescriptorFilter byApp(Boolean value);

    NutsDescriptorFilter byExtension(String apiVersion);
    NutsDescriptorFilter byRuntime(String apiVersion);

    NutsDescriptorFilter byCompanion(String apiVersion);

    NutsDescriptorFilter byApiVersion(String apiVersion);

    NutsDescriptorFilter byLockedIds(String ...ids);
}
