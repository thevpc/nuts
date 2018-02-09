package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsFile;
import net.vpc.app.nuts.NutsId;

import java.util.*;

public class NutsIdGraph {
    private Map<NutsId,List<NutsId>> allVertices =new LinkedHashMap<>();
    private Map<NutsId,NutsFile> files=new LinkedHashMap<>();
    private Map<String,Set<NutsId>> flatVersions=new LinkedHashMap<>();
    private Set<NutsId> roots=new LinkedHashSet<>();
    public NutsIdGraph() {
    }

    public Map<String,Set<NutsId>> resolveConflicts(){
        Map<String,Set<NutsId>> all=new LinkedHashMap<>();
        for (Map.Entry<String, Set<NutsId>> v : flatVersions.entrySet()) {
            if(v.getValue().size()>1){
                all.put(v.getKey(),new HashSet<>(v.getValue()));
            }
        }
        return all;
    }

    public void remove(NutsId id){
        files.remove(id);
        allVertices.remove(id);
        Set<NutsId> old = flatVersions.get(id.getFullName());
        if(old!=null){
            old.remove(id);
            if(old.isEmpty()){
                flatVersions.remove(id.getFullName());
            }
        }
        //now remove all vertex to this id
        List<NutsId> fromToToRemove=new ArrayList<>();
        for (Map.Entry<NutsId, List<NutsId>> v : allVertices.entrySet()) {
            for (NutsId to : v.getValue()) {
                if(id.equals(to)){
                    fromToToRemove.add(v.getKey());
                }
            }
        }
        for (NutsId nutsId : fromToToRemove) {
            List<NutsId> list = allVertices.get(nutsId);
            if(list!=null){
                list.remove(id);
                if(list.isEmpty()){
                    allVertices.remove(nutsId);
                }
            }
        }
    }

    public void addRoot(NutsId id){
        roots.add(id);
    }

    public boolean contains(NutsId id){
        return allVertices.containsKey(id);
    }

    public NutsFile getNutsFile(NutsId id){
        return files.get(id);
    }

    private void set(NutsFile from){
        NutsFile old = files.get(from.getId());
        if(old==null){
            files.put(from.getId(),from);
        }
    }

    public void add(NutsFile from, NutsFile to){
        set(from);
        set(to);
        List<NutsId> vertices = allVertices.get(from.getId());
        if(vertices==null){
            vertices=new ArrayList<>();
            allVertices.put(from.getId(),vertices);
        }
        vertices.add(to.getId());

        Set<NutsId> versions = flatVersions.get(from.getId().getFullName());
        if(versions==null){
            versions=new HashSet<>();
        }
        versions.add(from.getId());
        flatVersions.put(from.getId().getFullName(),versions);
    }

    public void visit(NutsId id,List<NutsFile> collected){
        Set<NutsId> visited=new HashSet<>();
        Stack<NutsId> stack=new Stack<>();
        stack.push(id);
        visited.add(id);
        while(!stack.isEmpty()){
            NutsId i = stack.pop();
            if(i!=null){
                NutsFile f=getNutsFile(i);
                if(f!=null){
                    collected.add(f);
                    List<NutsId> next = allVertices.get(i);
                    if(next!=null){
                        for (NutsId j : next) {
                            if(!visited.contains(j)){
                                visited.add(j);
                                stack.push(j);
                            }
                        }
                    }
                }
            }
        }
    }
}
