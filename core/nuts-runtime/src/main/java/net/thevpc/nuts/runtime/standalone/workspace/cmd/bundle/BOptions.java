package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import java.util.ArrayList;
import java.util.List;

class BOptions {
    List<String> ids = new ArrayList<>();
    List<String> lib = new ArrayList<>();
    boolean optional = false;
    String appVersion = null;
    String appName = null;
    String appTitle = null;
    String appDesc = null;
    String withTarget = null;
    BundleType format = BundleType.JAR;
    boolean clean = false;
    boolean embedded = false;
    boolean verbose = false;
    boolean reset = false;
    boolean yes = true;
}
