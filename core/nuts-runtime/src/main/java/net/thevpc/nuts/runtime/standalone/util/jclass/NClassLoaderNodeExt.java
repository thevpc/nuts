package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.NClassLoaderNode;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.io.NPath;

import java.util.ArrayList;
import java.util.List;

public class NClassLoaderNodeExt {
    public NClassLoaderNode node;
    public NId id;
    public NPath path;
    public boolean jfx;
    public List<String> requiredJfx =new ArrayList<>();
    public JavaClassByteCode.ModuleInfo moduleInfo;
    public String moduleName;
}
