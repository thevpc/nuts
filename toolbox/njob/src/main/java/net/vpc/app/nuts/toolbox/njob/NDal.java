package net.vpc.app.nuts.toolbox.njob;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsJsonFormat;
import net.vpc.app.nuts.toolbox.njob.model.Id;
import net.vpc.app.nuts.toolbox.njob.model.NJob;
import net.vpc.app.nuts.toolbox.njob.model.NTask;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NDal {
    private NutsApplicationContext context;
    private NutsJsonFormat json;

    public NDal(NutsApplicationContext context) {
        this.context = context;
        json = context.getWorkspace().formats().json();
        json.setCompact(false);
    }

    private Field getKeyField(Class o) {
        for (Field declaredField : o.getDeclaredFields()) {
            Id a = declaredField.getAnnotation(Id.class);
            if (a != null) {
                declaredField.setAccessible(true);
                return declaredField;
            }
        }
        throw new RuntimeException("@Id field Not found");
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
        return context.getVarFolder().resolve("db").resolve(entityName).resolve(id + ".json");
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
                SimpleDateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                while(true){
                    //test until we reach next millisecond
                    String nid=yyyyMMddHHmmssSSS.format(new Date());
                    Path f = getFile(getEntityName(NJob.class), nid);
                    if(!Files.exists(f)){
                        j.setId(nid);
                        break;
                    }
                }
            }
        }else if(o instanceof NTask){
            NTask j = (NTask) o;
            String ii=j.getId();
            if (ii==null){
                SimpleDateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                while(true){
                    //test until we reach next millisecond
                    String nid=yyyyMMddHHmmssSSS.format(new Date());
                    Path f = getFile(getEntityName(NTask.class), nid);
                    if(!Files.exists(f)){
                        j.setId(nid);
                        break;
                    }
                }
            }
        }
        Path objectFile = getObjectFile(o);
        if(!Files.isDirectory(objectFile.getParent())){
            try {
                Files.createDirectories(objectFile.getParent());
            } catch (IOException e) {
                throw new NutsIllegalArgumentException(context.getWorkspace(),"unable to create parent path");
            }
        }
        json.value(o).println(objectFile);
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
