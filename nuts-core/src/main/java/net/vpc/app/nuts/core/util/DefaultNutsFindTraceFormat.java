/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.util.LinkedHashMap;
import java.util.Map;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDescriptorFormat;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFormat;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsTraceFormat;
import net.vpc.app.nuts.NutsUnsupportedArgumentException;
import net.vpc.app.nuts.NutsWorkspace;
import static net.vpc.app.nuts.core.util.CoreNutsUtils.tracePlainNutsDefinition;

/**
 *
 * @author vpc
 */
public class DefaultNutsFindTraceFormat implements NutsTraceFormat {

    public static final NutsTraceFormat INSTANCE = new DefaultNutsFindTraceFormat();

    public NutsIdFormat getIdFormat(NutsWorkspace ws) {
        String k = DefaultNutsFindTraceFormat.class.getName() + "#NutsIdFormat";
        NutsIdFormat f = (NutsIdFormat) ws.getUserProperties().get(k);
        if (f == null) {
            f = ws.formatter().createIdFormat();
            ws.getUserProperties().put(k, f);
        }
        return f;
    }

    public NutsDescriptorFormat getDescriptorFormat(NutsWorkspace ws) {
        String k = DefaultNutsFindTraceFormat.class.getName() + "#NutsDescriptorFormat";
        NutsDescriptorFormat f = (NutsDescriptorFormat) ws.getUserProperties().get(k);
        if (f == null) {
            f = ws.formatter().createDescriptorFormat();
            ws.getUserProperties().put(k, f);
        }
        return f;
    }

    @Override
    public Object format(NutsId id, NutsOutputFormat type, NutsWorkspace ws) {
        if (type == null) {
            type = NutsOutputFormat.PLAIN;
        }
        switch (type) {
            case PLAIN: {
                return getIdFormat(ws).toString(id);
            }
            case PROPS: {
                return getIdFormat(ws).toString(id);
            }
            case JSON: {
                return getIdFormat(ws).toString(id);
            }
        }
        throw new NutsUnsupportedArgumentException(String.valueOf(type));
    }

    @Override
    public Object format(NutsDefinition def, NutsOutputFormat type, NutsWorkspace ws) {
        if (type == null) {
            type = NutsOutputFormat.PLAIN;
        }
        switch (type) {
            case PLAIN: {
                NutsIdFormat idFormat = ws.formatter().createIdFormat();
                return idFormat.toString(def.getId());
            }
            case PROPS: {
                NutsIdFormat idFormat = ws.formatter().createIdFormat();
                return idFormat.toString(def.getId());
            }
            case JSON: {
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
                    x.put("descriptor", getDescriptorFormat(ws).toString(def.getDescriptor()));
                    x.put("effective-descriptor", getDescriptorFormat(ws).toString(def.getEffectiveDescriptor()));
                }
                return x;
            }
        }
        throw new NutsUnsupportedArgumentException(String.valueOf(type));
    }

}
