package net.vpc.app.nuts.core.format;

import java.util.*;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsDescriptorFormat;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

public class DefaultNutsWorkspaceFormatManager implements NutsWorkspaceFormatManager {

    private final NutsWorkspace ws;

    public DefaultNutsWorkspaceFormatManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsIdFormat createIdFormat() {
        return new DefaultNutsIdFormat(ws);
    }

    @Override
    public NutsWorkspaceVersionFormat createWorkspaceVersionFormat() {
        return new DefaultVersionFormat(ws);
    }

    @Override
    public NutsWorkspaceInfoFormat createWorkspaceInfoFormat() {
        return new DefaultInfoFormat(ws);
    }

    @Override
    public NutsDescriptorFormat createDescriptorFormat() {
        return new DefaultNutsDescriptorFormat(ws);
    }

    @Override
    public NutsIncrementalFormat createIncrementalFormat(NutsOutputFormat format) {
        if (format == null) {
            format = NutsOutputFormat.PLAIN;
        }
        switch (format) {
            case JSON:
                return new DefaultSearchFormatJson(ws);
            case XML:
                return new DefaultSearchFormatXml(ws);
            case PLAIN:
                return new DefaultSearchFormatPlain(ws);
            case PROPS:
                return new DefaultSearchFormatProps(ws);
            case TREE:
                return new DefaultSearchFormatTree(ws);
            case TABLE:
                return new DefaultSearchFormatTable(ws);
        }
        throw new NutsUnsupportedArgumentException(ws, String.valueOf(format));
    }

    @Override
    public NutsTableFormat createTableFormat() {
        return new DefaultTableFormat(ws).setTerminalFormat(ws.io().getTerminalFormat());
    }

    @Override
    public NutsPropertiesFormat createPropertiesFormat() {
        return new DefaultPropertiesFormat(ws).setTerminalFormat(ws.io().getTerminalFormat());
    }

    @Override
    public NutsTreeFormat createTreeFormat() {
        return new DefaultTreeFormat(ws).setTerminalFormat(ws.io().getTerminalFormat());
    }

    @Override
    public NutsObjectFormat createObjectFormat(NutsSession session, Object value) {
        session = NutsWorkspaceUtils.validateSession(ws, session);
        NutsObjectFormat w = createObjectFormat(session.getOutputFormat(), value);
        w.session(session);
        return w;
    }

    @Override
    public NutsObjectFormat createObjectFormat(NutsOutputFormat format, Object value) {
        if (format == null) {
            format = NutsOutputFormat.PLAIN;
        }
        NutsObjectFormat ww = null;
        if (value instanceof Collection) {
            ww = new NutsObjectFormatBaseCollection(format, ws, (Collection) value);
        } else if (value instanceof Map) {
            ww = new NutsObjectFormatBaseMap(format, ws, (Map) value);
            return ww;
        } else {
            if (value == null || value.getClass().getName().startsWith("java")) {
                ww = new NutsObjectFormatBaseSimple(format, ws, value);
            } else {
                ww = new NutsObjectFormatBaseInspected(format, ws, value);
            }
        }
        ww.setTerminalFormat(ws.io().getTerminalFormat());
        return ww;
    }

}
