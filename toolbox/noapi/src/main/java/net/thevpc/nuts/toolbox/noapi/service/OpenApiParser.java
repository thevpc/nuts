package net.thevpc.nuts.toolbox.noapi.service;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsArrayElement;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementEntry;
import net.thevpc.nuts.elem.NutsObjectElement;
import net.thevpc.nuts.toolbox.noapi.model.ConfigVar;
import net.thevpc.nuts.toolbox.noapi.model.FieldInfo;
import net.thevpc.nuts.toolbox.noapi.model.TypeInfo;
import net.thevpc.nuts.toolbox.noapi.model.Vars;

import java.util.*;

public class OpenApiParser {

    public static Vars _fillVars(NutsObjectElement apiElement, Map<String, String> vars) {
        Map<String, String> m = new LinkedHashMap<>();

        NutsOptional<NutsObjectElement> v = apiElement.getObjectByPath("custom", "variables");
        if (v.isPresent()) {
            for (NutsElementEntry entry : v.get().entries()) {
                m.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        if (vars != null) {
            m.putAll(vars);
        }
        return new Vars(m);
    }

    public static List<ConfigVar> loadConfigVars(NutsObjectElement configElements, NutsObjectElement apiElements, Vars vars2, NutsSession session) {
        LinkedHashMap<String, ConfigVar> all = new LinkedHashMap<>();
        for (NutsElementEntry srv : apiElements.getObjectByPath("custom", "config", "variables").orElse(NutsObjectElement.ofEmpty(session)).entries()) {
            String id = srv.getKey().asString().get(session);
            String name = vars2.format(srv.getValue().asObject().get(session).getString("name").get(session));
            String example = vars2.format(srv.getValue().asObject().get(session).getString("example").get(session));
            String description = vars2.format(srv.getValue().asObject().get(session).getString("description").get(session));

            all.put(id, new ConfigVar(id, name, description, example, null, null));
        }
        if (configElements != null) {
            for (NutsElementEntry srv : configElements.getObjectByPath("variables").orElse(NutsObjectElement.ofEmpty(session)).entries()) {
                String id = srv.getKey().asString().get(session);
                String value = vars2.format(srv.getValue().asObject().get(session).getString("value").get(session));
                String observations = vars2.format(srv.getValue().asObject().get(session).getString("observations").get(session));
                ConfigVar f = all.get(id);
                if (f == null) {
                    f = new ConfigVar(id, id, id, id, value, observations);
                    all.put(id, f);
                } else {
                    f.setObservations(observations);
                    f.setValue(value);
                }
            }
        }
        return new ArrayList<>(all.values());
    }

    public TypeInfo parseOneType(NutsObjectElement value, String name0, NutsSession session) {
        NutsObjectElement v = value.asObject().get(session);
        TypeInfo tt = new TypeInfo();
        tt.setName(v.getString("name").orElse(name0));
        tt.setType(value.getString("type").orNull());
        if (NutsBlankable.isBlank(tt.getType())) {
            if (value.get("properties").orNull() != null) {
                tt.setType("object");
            } else if (value.get("items").orNull() != null) {
                tt.setType("array");
            } else if (
                    !NutsBlankable.isBlank(value.getString("$ref").orNull())
                            || !NutsBlankable.isBlank(value.getStringByPath("schema", "$ref").orNull())
            ) {
                tt.setType("ref");
            } else {
                tt.setType("string");
            }
        }
        tt.setSmartName(tt.getType());
        tt.setDescription(v.getString("description").orNull());
        tt.setSummary(v.getString("summary").orNull());
        tt.setExample(value.get("example").orNull());
        if (!NutsBlankable.isBlank(value.getString("$ref").orNull())) {
            tt.setRefLong(value.getString("$ref").orNull());
            tt.setRef(userNameFromRefValue(tt.getRefLong()));
            tt.setUserType("$ref");
            tt.setSmartName(tt.getRef());
        } else if (!NutsBlankable.isBlank(value.getStringByPath("schema", "$ref").orNull())) {
            tt.setRefLong(value.getStringByPath("schema", "$ref").orNull());
            tt.setRef(userNameFromRefValue(tt.getRefLong()));
            tt.setUserType("$ref");
            tt.setSmartName(tt.getRef());
        } else if ("array".equals(tt.getType())) {
            NutsObjectElement items = v.getObject("items").orNull();
            if(items==null){
                TypeInfo a=new TypeInfo();
                a.setType("string");
                a.setSmartName(a.getType());
                tt.setArrayComponentType(a);
                tt.setSmartName(a.getSmartName()+"[]");
            }else {
                TypeInfo a = parseOneType(items, null, session);
                tt.setArrayComponentType(a);
                tt.setSmartName(a.getSmartName()+"[]");
            }
            tt.setUserType(tt.getSmartName());
        } else if (value.get("properties").orNull() != null || "object".equals(tt.getType())) {
            Set<String> requiredSet = new HashSet<>();
            NutsArrayElement requiredElem = v.getArray("required").orNull();
            if (requiredElem != null) {
                for (NutsElement e : requiredElem) {
                    String a = e.asString().orElse("");
                    if (!NutsBlankable.isBlank(a)) {
                        a = a.trim();
                        requiredSet.add(a);
                    }
                }
            }
            NutsObjectElement a = v.getObject("properties").orNull();
            if (a != null) {
                for (NutsElementEntry p : a) {
                    FieldInfo ff = new FieldInfo();
                    ff.name = p.getKey().asString().orElse("").trim();
                    NutsObjectElement prop = p.getValue().asObject().get(session);
                    ff.description = prop.getString("description").orNull();
                    ff.summary = prop.getString("summary").orNull();
                    ff.example = prop.getString("example").orNull();
                    ff.required = requiredSet.contains(ff.name);
                    ff.schema = parseOneType(prop, null, session);
                    tt.getFields().add(ff);
                }
                return tt;
            }
        } else {
            tt.setFormat(value.getString("format").orNull());
            tt.setMinLength(value.getString("minLength").orNull());
            tt.setMaxLength(value.getString("maxLength").orNull());
            tt.setRefLong(value.getString("$ref").orNull());
            if (!NutsBlankable.isBlank(tt.getRefLong())) {
                tt.setRef(userNameFromRefValue(tt.getRefLong()));
            }
            if ("date".equals(tt.getFormat()) || "date-time".equals(tt.getFormat())) {
                tt.setUserType(tt.getFormat());
            } else if (!NutsBlankable.isBlank(tt.getRefLong())) {
                tt.setUserType(tt.getRef());
            } else if (NutsBlankable.isBlank(tt.getType())) {
                tt.setUserType("string");
            } else {
                tt.setUserType(tt.getType().trim().toLowerCase());
            }
            NutsArrayElement senum = value.getArray("enum").orElse(NutsArrayElement.ofEmpty(session));
            if (!senum.isEmpty()) {
                tt.setEnumValues(new ArrayList<>());
                if ("string".equals(tt.getUserType())) {
                    tt.setUserType("enum");
                }
                for (NutsElement ee : senum) {
                    tt.getEnumValues().add(ee.asString().get(session));
                }
            }
        }
        return tt;
    }

    public Map<String, TypeInfo> parseTypes(NutsObjectElement root, NutsSession session) {

        Map<String, TypeInfo> res = new LinkedHashMap<>();
        NutsObjectElement schemas = root.getObjectByPath("components", "schemas").orNull();
        if (schemas == null || schemas.isEmpty()) {
            return res;
        }
        for (NutsElementEntry entry : schemas) {
            String name0 = entry.getKey().asString().get(session);
            NutsElement value = entry.getValue();
            TypeInfo a = parseOneType(value.asObject().get(session), name0, session);
            if (a != null) {
                res.put(name0, a);
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
