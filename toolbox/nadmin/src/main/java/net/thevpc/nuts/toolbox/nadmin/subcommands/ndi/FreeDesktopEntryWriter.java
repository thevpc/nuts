package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;

import java.nio.file.Path;

public interface FreeDesktopEntryWriter {

    PathInfo[] writeShortcut(FreeDesktopEntry.Group descriptor, Path path, boolean doOverride, NutsId id);

    PathInfo[] writeDesktop(FreeDesktopEntry.Group descriptor, String fileName, boolean doOverride, NutsId id);

    PathInfo[] writeMenu(FreeDesktopEntry.Group descriptor, String fileName, boolean doOverride, NutsId id);


    PathInfo[] writeShortcut(FreeDesktopEntry descriptor, Path path,boolean doOverride, NutsId id);
    PathInfo[] writeDesktop(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id);

    PathInfo[] writeMenu(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id);
}
