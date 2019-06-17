/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.json;

import net.vpc.app.nuts.*;
import java.io.Writer;
import java.util.*;
import net.vpc.app.nuts.core.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.core.format.NutsObjectFormatBase;

/**
 *
 * @author vpc
 */
public class NutsObjectFormatJson extends NutsObjectFormatBase {

    final NutsOutputFormat t;
    final NutsWorkspace ws;
    private String rootName = "";
    private List<String> extraConfig = new ArrayList<>();
    private Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatJson(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.JSON.name().toLowerCase() + "-format");
        this.t = NutsOutputFormat.JSON;
        this.ws = ws;
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsArgument n = commandLine.peek();
        if (n != null) {
            NutsArgument a;
            if ((a = commandLine.nextString(DefaultPropertiesFormat.OPTION_MULTILINE_PROPERTY)) != null) {
                NutsArgument i = a.getArgumentValue();
                extraConfig.add(a.getString());
                addMultilineProperty(i.getStringKey(), i.getStringValue());
            } else {
                extraConfig.add(commandLine.next().getString());
            }
            return true;
        }
        return false;
    }

    @Override
    public void print(Writer w) {
        ws.format().json().set(getValue()).print(w);
    }

    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }
}
