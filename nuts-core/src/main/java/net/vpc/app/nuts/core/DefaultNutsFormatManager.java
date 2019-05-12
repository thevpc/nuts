package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.util.app.NutsDefaultWorkspaceCellFormat;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.DefaultNutsFindTraceFormatJson;
import net.vpc.app.nuts.core.util.DefaultNutsFindTraceFormatPlain;
import net.vpc.app.nuts.core.util.DefaultNutsFindTraceFormatProps;
import net.vpc.app.nuts.core.util.cmdline.format.DefaultPropertiesFormat;
import net.vpc.app.nuts.core.util.cmdline.format.DefaultTableFormat;
import net.vpc.app.nuts.core.util.cmdline.format.DefaultTreeFormat;

public class DefaultNutsFormatManager implements NutsFormatManager {

    private NutsWorkspace ws;

    public DefaultNutsFormatManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsIdFormat createIdFormat() {
        return new DefaultNutsIdFormat(ws);
    }

    @Override
    public NutsWorkspaceVersionFormat createWorkspaceVersionFormat() {
        return new DefaultNutsWorkspaceVersionFormat(ws);
    }

    @Override
    public NutsWorkspaceInfoFormat createWorkspaceInfoFormat() {
        return new DefaultNutsWorkspaceInfoFormat(ws);
    }

    @Override
    public NutsDescriptorFormat createDescriptorFormat() {
        return new DefaultNutsDescriptorFormat(ws);
    }

    @Override
    public NutsOutputListFormat createOutputListFormat(NutsOutputFormat format) {
        if (format == null) {
            format = NutsOutputFormat.PLAIN;
        }
        switch (format) {
            case JSON:
                return new DefaultNutsFindTraceFormatJson(ws);
            case PLAIN:
                return new DefaultNutsFindTraceFormatPlain(ws);
            case PROPS:
                return new DefaultNutsFindTraceFormatProps(ws);
        }
        throw new NutsUnsupportedArgumentException(String.valueOf(format));
    }

    @Override
    public NutsTableFormat createTableFormat() {
        return new DefaultTableFormat(new NutsDefaultWorkspaceCellFormat(ws));
    }

    @Override
    public NutsPropertiesFormat createPropertiesFormat() {
        return new DefaultPropertiesFormat();
    }

    @Override
    public NutsTreeFormat createTreeFormat() {
        return new DefaultTreeFormat();
    }

}
