package net.thevpc.nuts.boot;

import net.thevpc.nuts.NId;

import java.util.List;
import java.util.Set;

public class NIdCache {
    public NId baseId = null;
    public NId id = null;
    public String jar = null;
    public String expected = null;
    public boolean temp;
    public Set<NId> deps;
    public List<NIdCache> depsData;
}
