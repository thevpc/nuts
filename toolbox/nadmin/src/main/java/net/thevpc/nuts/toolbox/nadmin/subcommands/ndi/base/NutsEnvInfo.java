package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsVersion;

import java.nio.file.Path;

public interface NutsEnvInfo {
    Path getNutsJarPath();

    Path getBinFolder();
    Path getIncFolder();
    Path getNadminAppsFolder();

    Path getNutsApiAppsFolder();

    NutsDefinition getNutsApiDef();

    NutsId getNutsApiId();

    NutsVersion getNutsApiVersion();

    Path getWorkspaceLocation();
}
