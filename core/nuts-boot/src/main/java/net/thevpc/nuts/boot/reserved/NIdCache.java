package net.thevpc.nuts.boot.reserved;

import net.thevpc.nuts.boot.NIdBoot;

import java.util.List;
import java.util.Set;

public class NIdCache {
    public NIdBoot baseId = null;
    public NIdBoot id = null;
    public String jar = null;
    public String expected = null;
    public boolean temp;
    public Set<NIdBoot> deps;
    public List<NIdCache> depsData;
}
