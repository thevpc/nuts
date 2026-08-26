package net.thevpc.nuts.boot.internal.util;

import net.thevpc.nuts.boot.NBootDependency;

import java.util.List;
import java.util.Set;

public class NBootIdCache {
    public NBootDependency baseId = null;
    public NBootDependency id = null;
    public String jar = null;
    public String expected = null;
    public boolean temp;
    public Set<NBootDependency> deps;
    public List<NBootIdCache> depsData;
}
