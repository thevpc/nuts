package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi;

import net.thevpc.nuts.toolbox.nadmin.PathInfo;

import java.nio.file.Path;

public interface FreeDesktopEntryWriter {

    PathInfo[] writeDesktop(FreeDesktopEntry file, boolean doOverride);

    PathInfo[] writeMenu(FreeDesktopEntry file, String menuPath, boolean doOverride);
}
