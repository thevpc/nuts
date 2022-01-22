package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.njob.model.Id;
import net.thevpc.nuts.toolbox.njob.model.NJob;
import net.thevpc.nuts.toolbox.njob.model.NProject;
import net.thevpc.nuts.toolbox.njob.model.NTask;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.stream.Stream;

public class NJobConfigStore {
    private NutsApplicationContext context;
    private NutsElements json;
    private NutsPath dbPath;

    public NJobConfigStore(NutsApplicationContext applicationContext) {
        this.context = applicationContext;
        NutsSession session = applicationContext.getSession();
        json = NutsElements.of(session).json().setNtf(false);
        json.setCompact(false);
        //ensure we always consider the latest config version
        dbPath = applicationContext.getVersionFolder(NutsStoreLocation.CONFIG, NJobConfigVersions.CURRENT)
        .resolve("db");
    }

    private Field getKeyField(Class o) {
        for (Field declaredField : o.getDeclaredFields()) {
            Id a = declaredField.getAnnotation(Id.class);
            if (a != null) {
                declaredField.setAccessible(true);
                return declaredField;
            }
        }
        throw new RuntimeException("missing @Id field");
    }

    private Object getKey(Object o) {
        try {
            return getKeyField(o.getClass()).get(o);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String getEntityName(Class o) {
        return o.getSimpleName().toLowerCase();
    }

    private NutsPath getObjectFile(Object o) {
        return getFile(getEntityName(o.getClass()), getKey(o));
    }

    private NutsPath getFile(String entityName, Object id) {
        return dbPath.resolve(entityName).resolve(id + ".json");
    }

    public <T> Stream<T> search(Class<T> type) {
        NutsPath f = getFile(getEntityName(type), "any").getParent();
        NutsFunction<NutsPath, T> parse = NutsFunction.of(x -> json.parse(x, type), "parse");
        return f.list().filter(
                x -> x.isRegularFile() && x.getName().endsWith(".json"),
                        "isRegularFile() && matches(*.json"+")"
                )
                .map(parse,elem->elem.ofString("parse"))
                .filterNonNull().stream();
    }

    public <T> T load(Class<T> type, Object id) {
        NutsPath f = getFile(getEntityName(type), id);
        if (f.exists()) {
            return json.parse(f, type);
        }
        return null;
    }

    public void store(Object o) {
        if(o instanceof NJob) {
            NJob j = (NJob) o;
            String ii=j.getId();
            if (ii==null){
                j.setId(generateId(NJob.class));
            }
        }else if(o instanceof NTask){
            NTask j = (NTask) o;
            String ii=j.getId();
            if (ii==null){
                j.setId(generateId(NTask.class));
            }
        }else if(o instanceof NProject){
            NProject j = (NProject) o;
            String ii=j.getId();
            if (ii==null){
                j.setId(generateId(NProject.class));
            }
        }
        NutsPath objectFile = getObjectFile(o);
        objectFile.mkParentDirs();
        json.setValue(o).println(objectFile);
    }

    public String generateId(Class clz) {
//        SimpleDateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        while(true){
            //test until we reach next millisecond
            String nid= UUID.randomUUID().toString();
//                    yyyyMMddHHmmssSSS.format(new Date());
            NutsPath f = getFile(getEntityName(clz), nid);
            if(!f.exists()){
                return nid;
            }
        }
    }

    public boolean delete(Class entityName, Object id) {
        return delete(getEntityName(entityName), id);
    }

    public boolean delete(String entityName, Object id) {
        NutsPath f = getFile(entityName, id);
        if (f.exists()) {
            f.delete();
            return true;
        }
        return false;
    }
}
