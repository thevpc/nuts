package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi;

import java.util.List;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.CreateNutsScriptCommand;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.NutsEnvInfo;

public interface SystemNdi {
//    PathInfo[] persistConfigGlobal(NutsEnvInfo env, boolean createDesktop, boolean createMenu);
//
//    PathInfo[] persistConfigSpecial(String name, String fileName, NutsEnvInfo env, boolean createDesktop, boolean createMenu, boolean createShortcut);

    PathInfo[] createNutsScript(NdiScriptOptions options);

    void removeNutsScript(String id, NutsSession session,NutsEnvInfo env);

//    void addNutsWorkspaceScript(String preferredScriptName, NutsEnvInfo env);

    PathInfo[] switchWorkspace(NutsEnvInfo env, boolean createDesktop, boolean createMenu);

    boolean isNutsBootId(NutsId id);

    PathInfo[] createBootScripts(String preferredName, NutsEnvInfo env,
                                 boolean global,
                                 boolean desktop, boolean menu, boolean shortcut,
                                 String fileName
    );

    PathInfo[] createNutsScript(
            CreateNutsScriptCommand cmd,
//            List<String> idsToInstall, String switchWorkspaceLocation, String linkName, Boolean persistentConfig,
//            ArrayList<String> executorOptions, boolean env, boolean fetch, NutsExecutionType execType,
            NutsApplicationContext context);
}
