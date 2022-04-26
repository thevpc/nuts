package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.*;

import java.util.*;

public class OpenApiParser {

    public TypeInfo parseOneType(NutsObjectElement value, String name0,NutsSession session) {
        NutsObjectElement v = value.asObject().get(session);
        TypeInfo tt=new TypeInfo();
        String t = v.getString("type").get(session);
        String name = v.getString("name").get(session);
        if(name==null){
            name=name0;
        }
        tt.name= name;
        tt.type = value.getString("type").orNull();
        tt.description=v.getString("description").orNull();
        tt.summary=v.getString("summary").orNull();
        tt.example = value.get("example");
        if(!NutsBlankable.isBlank(value.getString("$ref"))) {
            tt.refLong = value.getString("$ref").orNull();
            tt.ref = userNameFromRefValue(tt.refLong);
            tt.userType = "$ref";
        }else if(!NutsBlankable.isBlank(value.getStringByPath("schema","$ref").orNull())){
            tt.refLong=value.getStringByPath("schema","$ref").orNull();
            tt.ref = userNameFromRefValue(tt.refLong);
            tt.userType = "$ref";
        } else if (value.get("properties") != null || "object".equals(t)) {
            Set<String> requiredSet = new HashSet<>();
            NutsArrayElement requiredElem = v.getArray("required").orNull();
            if (requiredElem != null) {
                for (NutsElement e : requiredElem) {
                    String a = e.asString().orElse("");
                    if(!NutsBlankable.isBlank(a)){
                        a=a.trim();
                        requiredSet.add(a);
                    }
                }
            }
            NutsObjectElement a = v.getObject("properties").orNull();
            if (a != null) {
                for (NutsElementEntry p : a) {
                    FieldInfo ff=new FieldInfo();
                    ff.name=p.getKey().asString().orElse("").trim();
                    NutsObjectElement prop = p.getValue().asObject().get(session);
                    ff.description = prop.getString("description").orNull();
                    ff.summary = prop.getString("summary").orNull();
                    ff.example = prop.getString("example").orNull();
                    ff.required=requiredSet.contains(ff.name);
                    ff.schema= parseOneType(prop,null,session);
                    tt.fields.add(ff);
                }
                return tt;
            }
        }else{
            tt.format = value.getString("format").orNull();
            tt.minLength = value.getString("minLength").orNull();
            tt.maxLength = value.getString("maxLength").orNull();
            tt.refLong = value.getString("$ref").orNull();
            if (!NutsBlankable.isBlank(tt.refLong)){
                tt.ref = userNameFromRefValue(tt.refLong);
            }
            if ("date".equals(tt.format) || "date-time".equals(tt.format)) {
                tt.userType = tt.format;
            } else if (!NutsBlankable.isBlank(tt.refLong)) {
                tt.userType = tt.ref;
            } else if (NutsBlankable.isBlank(tt.type)) {
                tt.userType = "string";
            } else {
                tt.userType=tt.type.trim().toLowerCase();
            }
            NutsArrayElement senum = value.getArray("enum").orElse(NutsArrayElement.ofEmpty(session));
            if(!senum.isEmpty()){
                tt.enumValues=new ArrayList<>();
                if("string".equals(tt.userType)) {
                    tt.userType = "enum";
                }
                for (NutsElement ee : senum) {
                    tt.enumValues.add(ee.asString().get(session));
                }
            }
        }
        return tt;
    }

    public Map<String,TypeInfo> parseTypes(NutsObjectElement root,NutsSession session) {

        Map<String,TypeInfo> res=new LinkedHashMap<>();
        NutsObjectElement schemas = root.getObjectByPath("components","schemas").get(session);
        if (schemas.isEmpty()) {
            return res;
        }
        for (NutsElementEntry entry : schemas) {
            String name0 = entry.getKey().asString().get(session);
            NutsElement value = entry.getValue();
            TypeInfo a = parseOneType(value.asObject().get(session), name0,session);
            if(a!=null){
                res.put(name0,a);
            }
        }
        return res;
    }


    public String userNameFromRefValue(String dRef) {
        if (dRef != null) {
            if (dRef.startsWith("#/components/schemas/")) {
                return dRef.substring("#/components/schemas/".length());
            }
        }
        return dRef;
    }
}
