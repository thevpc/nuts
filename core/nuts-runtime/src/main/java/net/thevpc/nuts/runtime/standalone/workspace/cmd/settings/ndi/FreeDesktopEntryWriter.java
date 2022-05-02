package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;

public interface FreeDesktopEntryWriter {

    PathInfo[] writeShortcut(FreeDesktopEntry.Group descriptor, NutsPath path, boolean doOverride, NutsId id);

    PathInfo[] writeDesktop(FreeDesktopEntry.Group descriptor, String fileName, boolean doOverride, NutsId id);

    PathInfo[] writeMenu(FreeDesktopEntry.Group descriptor, String fileName, boolean doOverride, NutsId id);


    PathInfo[] writeShortcut(FreeDesktopEntry descriptor, NutsPath path, boolean doOverride, NutsId id);

    PathInfo[] writeDesktop(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id);

    PathInfo[] writeMenu(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id);
}
