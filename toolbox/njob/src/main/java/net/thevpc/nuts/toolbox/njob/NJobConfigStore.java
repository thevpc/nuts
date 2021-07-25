package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.njob.model.Id;
import net.thevpc.nuts.toolbox.njob.model.NJob;
import net.thevpc.nuts.toolbox.njob.model.NProject;
import net.thevpc.nuts.toolbox.njob.model.NTask;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NJobConfigStore {
    private NutsApplicationContext context;
    private NutsElementFormat json;
    private Path dbPath;

    public NJobConfigStore(NutsApplicationContext applicationContext) {
        this.context = applicationContext;
        json = applicationContext.getWorkspace().elem().setContentType(NutsContentType.JSON);
        json.setCompact(false);
        //ensure we always consider the latest config version
        dbPath = Paths.get(applicationContext.getVersionFolderFolder(NutsStoreLocation.CONFIG, NJobConfigVersions.CURRENT))
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

    private Path getObjectFile(Object o) {
        return getFile(getEntityName(o.getClass()), getKey(o));
    }

    private Path getFile(String entityName, Object id) {
        return dbPath.resolve(entityName).resolve(id + ".json");
    }

    public <T> Stream<T> search(Class<T> type) {
        Path f = getFile(getEntityName(type), "any").getParent();
        try {
            if (!Files.isDirectory(f)) {
                return Collections.<T>emptyList().stream();
            }
            return StreamSupport.stream(Files.newDirectoryStream(f, x -> Files.isRegularFile(x) && x.toString().endsWith(".json"))
                    .spliterator(), false)
                    .map(x -> {
                        try {
                            return json.parse(x, type);
                        } catch (Exception ex) {
                            return null;
                        }
                    }).filter(x -> x != null);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public <T> T load(Class<T> type, Object id) {
        Path f = getFile(getEntityName(type), id);
        if (Files.exists(f)) {
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
        Path objectFile = getObjectFile(o);
        if(!Files.isDirectory(objectFile.getParent())){
            try {
                Files.createDirectories(objectFile.getParent());
            } catch (IOException e) {
                throw new NutsIllegalArgumentException(context.getSession(),NutsMessage.cstyle("unable to create parent path"));
            }
        }
        json.setValue(o).println(objectFile);
    }

    public String generateId(Class clz) {
//        SimpleDateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        while(true){
            //test until we reach next millisecond
            String nid= UUID.randomUUID().toString();
//                    yyyyMMddHHmmssSSS.format(new Date());
            Path f = getFile(getEntityName(clz), nid);
            if(!Files.exists(f)){
                return nid;
            }
        }
    }

    public boolean delete(Class entityName, Object id) {
        return delete(getEntityName(entityName), id);
    }

    public boolean delete(String entityName, Object id) {
        Path f = getFile(entityName, id);
        if (Files.exists(f)) {
            try {
                Files.delete(f);
                return true;
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return false;
    }
}
