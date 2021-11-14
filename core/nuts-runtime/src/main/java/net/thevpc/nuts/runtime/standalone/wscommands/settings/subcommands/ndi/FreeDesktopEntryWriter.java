package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;

public interface FreeDesktopEntryWriter {

    PathInfo[] writeShortcut(FreeDesktopEntry.Group descriptor, NutsPath path, boolean doOverride, NutsId id);

    PathInfo[] writeDesktop(FreeDesktopEntry.Group descriptor, String fileName, boolean doOverride, NutsId id);

    PathInfo[] writeMenu(FreeDesktopEntry.Group descriptor, String fileName, boolean doOverride, NutsId id);


    PathInfo[] writeShortcut(FreeDesktopEntry descriptor, NutsPath path, boolean doOverride, NutsId id);

    PathInfo[] writeDesktop(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id);

    PathInfo[] writeMenu(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id);
}
