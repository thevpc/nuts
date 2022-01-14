package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.NutsClassLoaderNode;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsPath;

public class NutsClassLoaderNodeExt {
    public NutsClassLoaderNode node;
    public NutsId id;
    public NutsPath path;
    public boolean jfx;
    public JavaClassByteCode.ModuleInfo moduleInfo;
    public String moduleName;
}
