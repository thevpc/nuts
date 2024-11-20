package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NBlankable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class NanoDBTableDefinitionBuilderFromBean<T> {
    private final NanoDB db;
    private final Class<T> cls;
    private boolean addAllFields;
    private Map<String, Field> currFields;
    private final Set<String> serializedFields = new LinkedHashSet<>();
    private final Set<String> indexFields = new LinkedHashSet<>();
    private boolean nullable=true;
    private String name;
    private NWorkspace workspace;


    public NanoDBTableDefinitionBuilderFromBean(Class<T> cls, NanoDB db, NWorkspace workspace) {
        this.cls = cls;
        this.db = db;
        this.workspace = workspace;
    }
    private Map<String, Field> getCurrFields(){
        if(currFields==null){
            LinkedHashMap currFields=new LinkedHashMap<>();
            Class cc = cls;
            while (cc != null && cc != Object.class) {
                for (Field f : cc.getDeclaredFields()) {
                    if (!currFields.containsKey(f.getName())) {
                        int m = f.getModifiers();
                        if (!Modifier.isFinal(m) && !Modifier.isStatic(m) && !Modifier.isTransient(m)) {
                            f.setAccessible(true);
                            currFields.put(f.getName(), f);
                        }
                    }
                }
                cc = cc.getSuperclass();
            }
            this.currFields=currFields;
        }
        return currFields;
    }

    public boolean isNullable() {
        return nullable;
    }

    public NanoDBTableDefinitionBuilderFromBean<T> setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public NanoDBTableDefinitionBuilderFromBean<T> addIndices(String... fields) {
        indexFields.addAll(Arrays.asList(fields));
        return this;
    }

    public String getName() {
        return name;
    }

    public NanoDBTableDefinitionBuilderFromBean<T> setName(String name) {
        this.name = name;
        return this;
    }

    public NanoDBTableFile<T> getOrCreate(){
        String name=this.name;
        if(NBlankable.isBlank(name)){
            name=cls.getSimpleName();
        }
        NanoDBTableFile t = db.findTable(name);
        if(t!=null){
            return t;
        }
        return create();
    }

    public NanoDBTableFile<T> create(){
        NanoDBTableDefinition<T> t = build();
        return db.createTable(t);
    }

    public NanoDBTableDefinition<T> build(){
        Map<String, Field> currFields = getCurrFields();
        Set<String> serializedFields = new LinkedHashSet<>();
        serializedFields.addAll(this.serializedFields);
        if(addAllFields || serializedFields.isEmpty()){
            serializedFields.addAll(currFields.keySet());
        }
        String name=this.name;
        if(NBlankable.isBlank(name)){
            name=cls.getSimpleName();
        }
        NanoDBSerializerForBean<T> s = new NanoDBSerializerForBean<>(cls, db.getSerializers(), serializedFields);
        List<NanoDBDefaultIndexDefinition<T>> defs = new ArrayList<>();
        for (String field : indexFields) {
            if(currFields.containsKey(field)){
                Field f = currFields.get(field);
                defs.add(new NanoDBFieldIndexDefinition<T>(f));
            }
        }
        return new NanoDBTableDefinition<T>(
                name,
                cls,
                nullable?new NanoDBSerializerForNullable<>(s) : s,
                defs.toArray(new NanoDBIndexDefinition[0])
        );
    }

    public NanoDBTableDefinitionBuilderFromBean<T> addAllFields() {
        this.addAllFields=true;
        return this;
    }
}
