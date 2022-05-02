package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.boot.NutsClassLoaderNode;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.io.NutsPath;

import java.util.ArrayList;
import java.util.List;

public class NutsClassLoaderNodeExt {
    public NutsClassLoaderNode node;
    public NutsId id;
    public NutsPath path;
    public boolean jfx;
    public List<String> requiredJfx =new ArrayList<>();
    public JavaClassByteCode.ModuleInfo moduleInfo;
    public String moduleName;
}
