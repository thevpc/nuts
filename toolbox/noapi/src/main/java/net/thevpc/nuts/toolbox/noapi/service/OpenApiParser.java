package net.thevpc.nuts.toolbox.noapi.service;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.toolbox.noapi.model.ConfigVar;
import net.thevpc.nuts.toolbox.noapi.model.FieldInfo;
import net.thevpc.nuts.toolbox.noapi.model.TypeInfo;
import net.thevpc.nuts.toolbox.noapi.model.Vars;
import net.thevpc.nuts.util.NOptional;

import java.util.*;

public class OpenApiParser {

    public static Vars _fillVars(NObjectElement apiElement, Map<String, String> vars) {
        Map<String, String> m = new LinkedHashMap<>();

        NOptional<NObjectElement> v = apiElement.getObjectByPath("custom", "variables");
        if (v.isPresent()) {
            for (NElementEntry entry : v.get().entries()) {
                m.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        if (vars != null) {
            m.putAll(vars);
        }
        return new Vars(m);
    }

    public static List<ConfigVar> loadConfigVars(NObjectElement configElements, NObjectElement apiElements, Vars vars2, NSession session) {
        LinkedHashMap<String, ConfigVar> all = new LinkedHashMap<>();
        for (NElementEntry srv : apiElements.getObjectByPath("custom", "config", "variables").orElse(NObjectElement.ofEmpty(session)).entries()) {
            String id = srv.getKey().asString().get(session);
            String name = vars2.format(srv.getValue().asObject().get(session).getString("name").get(session));
            String example = vars2.format(srv.getValue().asObject().get(session).getString("example").get(session));
            String description = vars2.format(srv.getValue().asObject().get(session).getString("description").get(session));

            all.put(id, new ConfigVar(id, name, description, example, null, null));
        }
        if (configElements != null) {
            for (NElementEntry srv : configElements.getObjectByPath("variables").orElse(NObjectElement.ofEmpty(session)).entries()) {
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

    public TypeInfo parseOneType(NObjectElement value, String name0, NSession session, Map<String, TypeInfo> allTypes) {
        NObjectElement v = value.asObject().get(session);
        TypeInfo tt = new TypeInfo();
        tt.setName(v.getString("name").orElse(name0));
        tt.setType(value.getString("type").orNull());
        if (NBlankable.isBlank(tt.getType())) {
            if (value.get("properties").orNull() != null) {
                tt.setType("object");
            } else if (value.get("items").orNull() != null) {
                tt.setType("array");
            } else if (
                    !NBlankable.isBlank(value.getString("$ref").orNull())
                            || !NBlankable.isBlank(value.getStringByPath("schema", "$ref").orNull())
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
        if (!NBlankable.isBlank(value.getString("$ref").orNull())) {
            tt.setRefLong(value.getString("$ref").orNull());
            tt.setRef(userNameFromRefValue(tt.getRefLong()));
            tt.setUserType("$ref");
            tt.setSmartName(tt.getRef());
        } else if (!NBlankable.isBlank(value.getStringByPath("schema", "$ref").orNull())) {
            tt.setRefLong(value.getStringByPath("schema", "$ref").orNull());
            tt.setRef(userNameFromRefValue(tt.getRefLong()));
            tt.setUserType("$ref");
            tt.setSmartName(tt.getRef());
        } else if ("array".equals(tt.getType())) {
            NObjectElement items = v.getObject("items").orNull();
            if (items == null) {
                TypeInfo a = new TypeInfo();
                a.setType("string");
                a.setSmartName(a.getType());
                tt.setArrayComponentType(a);
                TypeInfo refType = allTypes.get(a.getSmartName());
                tt.setSmartName(a.getSmartName() + "[]");
                Object e = a.getExample();
                if (e == null && refType != null) {
                    e = refType.getExample();
                }
                if (e != null) {
                    if(e instanceof NElement){
                        tt.setExample(NElements.of(session).ofArray().add((NElement) e).build());
                    }else {
                        tt.setExample(Arrays.asList(e));
                    }
                }
            } else {
                TypeInfo a = parseOneType(items, null, session, allTypes);
                tt.setArrayComponentType(a);
                tt.setSmartName(a.getSmartName() + "[]");
                TypeInfo refType = allTypes.get(a.getSmartName());
                Object e = a.getExample();
                if (e == null && refType != null) {
                    e = refType.getExample();
                }
                if (e != null) {
                    if(e instanceof NElement){
                        tt.setExample(NElements.of(session).ofArray().add((NElement) e).build());
                    }else {
                        tt.setExample(Arrays.asList(e));
                    }
                }
            }
            tt.setUserType(tt.getSmartName());
        } else if (value.get("properties").orNull() != null || "object".equals(tt.getType())) {
            Set<String> requiredSet = new HashSet<>();
            NArrayElement requiredElem = v.getArray("required").orNull();
            if (requiredElem != null) {
                for (NElement e : requiredElem) {
                    String a = e.asString().orElse("");
                    if (!NBlankable.isBlank(a)) {
                        a = a.trim();
                        requiredSet.add(a);
                    }
                }
            }
            NObjectElement a = v.getObject("properties").orNull();
            if (a != null) {
                for (NElementEntry p : a) {
                    FieldInfo ff = new FieldInfo();
                    ff.name = p.getKey().asString().orElse("").trim();
                    NObjectElement prop = p.getValue().asObject().get(session);
                    ff.description = prop.getString("description").orNull();
                    ff.summary = prop.getString("summary").orNull();
                    ff.example = prop.getString("example").orNull();
                    ff.required = requiredSet.contains(ff.name);
                    ff.schema = parseOneType(prop, null, session, allTypes);
                    tt.getFields().add(ff);
                }
                return tt;
            }
        } else {
            tt.setFormat(value.getString("format").orNull());
            tt.setMinLength(value.getString("minLength").orNull());
            tt.setMaxLength(value.getString("maxLength").orNull());
            tt.setRefLong(value.getString("$ref").orNull());
            if (!NBlankable.isBlank(tt.getRefLong())) {
                tt.setRef(userNameFromRefValue(tt.getRefLong()));
            }
            if ("date".equals(tt.getFormat()) || "date-time".equals(tt.getFormat())) {
                tt.setUserType(tt.getFormat());
            } else if (!NBlankable.isBlank(tt.getRefLong())) {
                tt.setUserType(tt.getRef());
            } else if (NBlankable.isBlank(tt.getType())) {
                tt.setUserType("string");
            } else {
                tt.setUserType(tt.getType().trim().toLowerCase());
            }
            NArrayElement senum = value.getArray("enum").orElse(NArrayElement.ofEmpty(session));
            if (!senum.isEmpty()) {
                tt.setEnumValues(new ArrayList<>());
                if ("string".equals(tt.getUserType())) {
                    tt.setUserType("enum");
                }
                for (NElement ee : senum) {
                    tt.getEnumValues().add(ee.asString().get(session));
                }
            }
        }
        return tt;
    }

    public Map<String, TypeInfo> parseTypes(NObjectElement root, NSession session) {

        Map<String, TypeInfo> res = new LinkedHashMap<>();
        NObjectElement schemas = root.getObjectByPath("components", "schemas").orNull();
        if (schemas == null || schemas.isEmpty()) {
            return res;
        }
        for (NElementEntry entry : schemas) {
            String name0 = entry.getKey().asString().get(session);
            NElement value = entry.getValue();
            TypeInfo a = parseOneType(value.asObject().get(session), name0, session, res);
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
