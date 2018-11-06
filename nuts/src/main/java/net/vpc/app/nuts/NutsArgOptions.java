package net.vpc.app.nuts;

import java.util.ArrayList;
import java.util.List;

public class NutsArgOptions {
    String workspace = null;
    boolean version = false;
    boolean doupdate = false;
    boolean checkupdates = false;
    String applyUpdatesFile = null;
    boolean perf = false;
    boolean showHelp = false;
    boolean showLicense = false;
    List<String> args = new ArrayList<>();
    NutsBootOptions bootOptions = new NutsBootOptions();
    NutsWorkspaceCreateOptions workspaceCreateOptions = new NutsWorkspaceCreateOptions();
}
