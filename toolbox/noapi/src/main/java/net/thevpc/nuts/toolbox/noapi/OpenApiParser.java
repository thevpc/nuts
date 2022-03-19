package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.*;

import java.util.*;

public class OpenApiParser {

    public TypeInfo parseOneType(NutsObjectElement value, String name0) {
        NutsObjectElement v = value.asObject();
        TypeInfo tt=new TypeInfo();
        String t = v.getString("type");
        String name = v.getString("name");
        if(name==null){
            name=name0;
        }
        tt.name= name;
        tt.type = value.getSafeString("type");
        tt.description=v.getSafeString("description");
        tt.summary=v.getSafeString("summary");
        tt.example = value.get("example");
        if(!NutsBlankable.isBlank(value.getSafeString("$ref"))) {
            tt.refLong = value.getSafeString("$ref");
            tt.ref = userNameFromRefValue(tt.refLong);
            tt.userType = "$ref";
        }else if(!NutsBlankable.isBlank(value.getSafeObject("schema").getSafeString("$ref"))){
            tt.refLong=value.getSafeObject("schema").getSafeString("$ref");
            tt.ref = userNameFromRefValue(tt.refLong);
            tt.userType = "$ref";
        } else if (value.get("properties") != null || "object".equals(t)) {
            Set<String> requiredSet = new HashSet<>();
            NutsArrayElement requiredElem = v.getArray("required");
            if (requiredElem != null) {
                for (NutsElement e : requiredElem) {
                    String a = e.asSafeString("");
                    if(!NutsBlankable.isBlank(a)){
                        a=a.trim();
                        requiredSet.add(a);
                    }
                }
            }
            NutsObjectElement a = v.getObject("properties");
            if (a != null) {
                for (NutsElementEntry p : a) {
                    FieldInfo ff=new FieldInfo();
                    ff.name=p.getKey().asString().trim();
                    NutsObjectElement prop = p.getValue().asObject();
                    ff.description = prop.getSafeString("description");
                    ff.summary = prop.getSafeString("summary");
                    ff.example = prop.getSafeString("example");
                    ff.required=requiredSet.contains(ff.name);
                    ff.schema= parseOneType(prop,null);
                    tt.fields.add(ff);
                }
                return tt;
            }
        }else{
            tt.format = value.getSafeString("format");
            tt.minLength = value.getSafeString("minLength");
            tt.maxLength = value.getSafeString("maxLength");
            tt.refLong = value.getSafeString("$ref");
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
            NutsArrayElement senum = value.getSafeArray("enum");
            if(!senum.isEmpty()){
                tt.enumValues=new ArrayList<>();
                if("string".equals(tt.userType)) {
                    tt.userType = "enum";
                }
                for (NutsElement ee : senum) {
                    tt.enumValues.add(ee.asString());
                }
            }
        }
        return tt;
    }

    public Map<String,TypeInfo> parseTypes(NutsObjectElement root) {
        Map<String,TypeInfo> res=new LinkedHashMap<>();
        NutsObjectElement schemas = root.getSafeObject("components").getSafeObject("schemas");
        if (schemas.isEmpty()) {
            return res;
        }
        for (NutsElementEntry entry : schemas) {
            String name0 = entry.getKey().asString();
            NutsElement value = entry.getValue();
            TypeInfo a = parseOneType(value.asObject(), name0);
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
