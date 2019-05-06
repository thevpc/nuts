/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsWorkspace;
import static net.vpc.app.nuts.core.util.CoreNutsUtils.tracePlainNutsDefinition;
import static net.vpc.app.nuts.core.util.NutsWorkspaceUtils.getIdFormat;

/**
 *
 * @author vpc
 */
public class CanonicalBuilder {

    private NutsWorkspace ws;

    private boolean convertId;
    private boolean convertDesc;

    public CanonicalBuilder(NutsWorkspace ws) {
        this.ws = ws;
    }

    public boolean isConvertId() {
        return convertId;
    }

    public CanonicalBuilder setConvertId(boolean convertId) {
        this.convertId = convertId;
        return this;
    }

    public boolean isConvertDesc() {
        return convertDesc;
    }

    public CanonicalBuilder setConvertDesc(boolean convertDesc) {
        this.convertDesc = convertDesc;
        return this;
    }

    /**
     * return either Map,List or simple (primitive,string)
     *
     * @param ws
     * @param anyObject
     * @return
     */
    
    public Object toCanonical(Object anyObject) {
        if (anyObject == null) {
            return null;
        }
        if (anyObject instanceof Enum || anyObject instanceof Boolean || anyObject instanceof Number || anyObject instanceof Date || anyObject.getClass().isPrimitive()) {
            return anyObject;
        }
        if (anyObject.getClass().isArray()) {
            List<Object> rr = new ArrayList<>();
            int len = Array.getLength(anyObject);
            for (int i = 0; i < len; i++) {
                rr.add(toCanonical(Array.get(anyObject, i)));
            }
            return rr;
        } else if (anyObject instanceof Collection) {
            List<Object> rr = new ArrayList<>();
            for (Object item : ((Collection) anyObject)) {
                rr.add(toCanonical(item));
            }
            return rr;
        } else if (anyObject instanceof Map) {
            Map<Object, Object> rr = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> ee : ((Map<Object, Object>) anyObject).entrySet()) {
                rr.put(toCanonical(ee.getKey()), toCanonical(ee.getValue()));
            }
            return rr;
        } else if (anyObject instanceof NutsDefinition) {
            NutsDefinition def = (NutsDefinition) anyObject;
            Map<String, Object> x = new LinkedHashMap<>();
            x.put("id", tracePlainNutsDefinition(ws, def));
            if (def.getContent() != null) {
                if (def.getContent().getPath() != null) {
                    x.put("path", def.getContent().getPath().toString());
                }
                x.put("cached", def.getContent().isCached());
                x.put("tomporary", def.getContent().isTemporary());
            }
            if (def.getInstallation() != null) {
                if (def.getInstallation().getInstallFolder() != null) {
                    x.put("install-folder", def.getInstallation().getInstallFolder().toString());
                }
                x.put("installed", def.getInstallation().isInstalled());
                x.put("just-installed", def.getInstallation().isJustInstalled());
            }
            if (def.getRepository() != null) {
                x.put("repository-name", def.getRepository().config().name());
                x.put("repository-uuid", def.getRepository().config().uuid());
            }
            if (def.getDescriptor() != null) {
                x.put("descriptor", toCanonical(def.getDescriptor()));
//                x.put("effective-descriptor", toCanonical(ws,def.getEffectiveDescriptor()));
            }
            return x;
        } else if (anyObject instanceof NutsDescriptor) {
            if (convertDesc) {
                String s = ws.io().json().pretty().toJsonString(anyObject);
                return ws.io().json().read(new StringReader(s), Map.class);
            }
            return anyObject;
        } else if (anyObject instanceof NutsId) {
            if (convertId) {
                return getIdFormat(ws).toString((NutsId) anyObject);
            }
            return anyObject;
        } else {
            return anyObject;
        }
    }
}
