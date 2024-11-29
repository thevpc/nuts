package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NBootId;

import java.util.List;
import java.util.Set;

public class NBootIdCache {
    public NBootId baseId = null;
    public NBootId id = null;
    public String jar = null;
    public String expected = null;
    public boolean temp;
    public Set<NBootId> deps;
    public List<NBootIdCache> depsData;
}
