package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.FreeDesktopEntry;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.FreeDesktopEntryWriter;

import java.nio.file.Path;

public abstract class AbstractFreeDesktopEntryWriter implements FreeDesktopEntryWriter {

    protected String ensureName(String input,String defaultName,String extension){
        String p=input;
        if(p==null||p.isEmpty()){
            p=defaultName;
        }
        if(!p.endsWith("."+extension)){
            p=p+"."+extension;
        }
        return p;
    }

    @Override
    public PathInfo[] writeShortcut(FreeDesktopEntry.Group descr, Path path, boolean doOverride, NutsId id) {
        FreeDesktopEntry e = new FreeDesktopEntry();
        e.add(descr);
        return writeShortcut(e, path, doOverride,id);
    }

    public PathInfo[] writeDesktop(FreeDesktopEntry.Group descriptor, String fileName, boolean doOverride, NutsId id) {
        FreeDesktopEntry e = new FreeDesktopEntry();
        e.add(descriptor);
        return writeDesktop(e, fileName, doOverride,id);
    }

    @Override
    public PathInfo[] writeMenu(FreeDesktopEntry.Group descriptor, String fileName, boolean doOverride, NutsId id) {
        FreeDesktopEntry e = new FreeDesktopEntry();
        e.add(descriptor);
        return writeMenu(e, fileName, doOverride,id);
    }
}
