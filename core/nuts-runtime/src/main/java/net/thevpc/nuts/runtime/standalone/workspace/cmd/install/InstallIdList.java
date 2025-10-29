package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.artifact.NId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InstallIdList {

    boolean emptyCommand = true;
    Map<String, InstallIdInfo> visited = new LinkedHashMap<>();
    InstallCache cache=new InstallCache();

    public InstallIdList() {
    }

    public boolean isVisited(NId id) {
        return visited.containsKey(InstallIdCacheItem.normalizeId(id).toString());
    }

    public List<NId> ids(Predicate<InstallIdInfo> filter) {
        return infos().stream().filter(filter).map(x -> x.id).collect(Collectors.toList());
    }

    public List<InstallIdInfo> infos(Predicate<InstallIdInfo> filter) {
        if (filter == null) {
            return infos();
        }
        return infos().stream().filter(filter).collect(Collectors.toList());
    }

    public List<InstallIdInfo> infos() {
        return new ArrayList<>(visited.values());
    }

    public InstallIdInfo addAsDeployed(NId id,InstallFlags flags) {
        flags=flags.copy();
        cache.get(id);
        String sid = InstallIdCacheItem.normalizeId(id).toString();
        InstallIdInfo old = visited.get(sid);
        if(old!=null){
            old.flags.merge(flags);
            old.flags.deployOnly=true;
            return old;
        }
        InstallIdInfo ii = new InstallIdInfo();
        ii.id = id;
        ii.sid = sid;
        ii.flags = flags;
        ii.flags.deployOnly=true;
        visited.put(ii.sid, ii);
        return ii;
    }

    public InstallIdInfo addAsRequired(NId id,NId forId,InstallFlags flags) {
        flags=flags.copy();
        cache.get(id);
        String sid = InstallIdCacheItem.normalizeId(id).toString();
        InstallIdInfo old = visited.get(sid);
        if(old!=null){
            old.flags.merge(flags);
            old.flags.require=true;
            old.requiredForIds.add(forId);
            return old;
        }
        InstallIdInfo ii = new InstallIdInfo();
        ii.id = id;
        ii.sid = sid;
        ii.flags = flags;
        ii.flags.require=true;
        ii.requiredForIds=new ArrayList<>();
        ii.requiredForIds.add(forId);
        visited.put(ii.sid, ii);
        return ii;
    }

    public InstallIdInfo addAsInstalled(NId id, InstallFlags flags) {
        flags=flags.copy();
        String sid = InstallIdCacheItem.normalizeId(id).toString();
        InstallIdInfo old = visited.get(sid);
        if(old!=null){
            old.flags.merge(flags);
            old.flags.install=true;
            return old;
        }
        cache.get(id);
        emptyCommand = false;
        InstallIdInfo ii = new InstallIdInfo();
        ii.id = id;
        ii.sid = sid;

        ii.flags = flags;
        ii.flags.install=true;
        visited.put(ii.sid, ii);
        return ii;
    }

    public InstallIdInfo addAsUninstalled(NId id, InstallFlags flags) {
        flags=flags.copy();
        String sid = InstallIdCacheItem.normalizeId(id).toString();
        InstallIdInfo old = visited.get(sid);
        if(old!=null){
            old.flags.merge(flags);
            return old;
        }
        cache.get(id);
        emptyCommand = false;
        InstallIdInfo ii = new InstallIdInfo();
        ii.id = id;
        ii.sid = sid;

        ii.flags = flags;
        visited.put(ii.sid, ii);
        return ii;
    }

    public InstallIdInfo get(NId id) {
        return visited.get(InstallIdCacheItem.normalizeId(id).toString());
    }
}
