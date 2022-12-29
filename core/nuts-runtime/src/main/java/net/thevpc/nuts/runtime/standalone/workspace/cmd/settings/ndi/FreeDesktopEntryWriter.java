package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;

public interface FreeDesktopEntryWriter {

    PathInfo[] writeShortcut(FreeDesktopEntry.Group descriptor, NPath path, boolean doOverride, NId id);

    PathInfo[] writeDesktop(FreeDesktopEntry.Group descriptor, String fileName, boolean doOverride, NId id);

    PathInfo[] writeMenu(FreeDesktopEntry.Group descriptor, String fileName, boolean doOverride, NId id);


    PathInfo[] writeShortcut(FreeDesktopEntry descriptor, NPath path, boolean doOverride, NId id);

    PathInfo[] writeDesktop(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NId id);

    PathInfo[] writeMenu(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NId id);
}
