/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.format.json;

import net.vpc.app.nuts.*;

import java.io.PrintStream;
import java.io.Writer;
import java.util.*;

import net.vpc.app.nuts.runtime.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.runtime.format.NutsObjectFormatBase;

/**
 * @author vpc
 */
public class NutsObjectFormatJson extends NutsObjectFormatBase {

    private final NutsOutputFormat t;
    private final NutsWorkspace ws;
    private final String rootName = "";
    private final List<String> extraConfig = new ArrayList<>();
    private final Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatJson(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.JSON.id() + "-format");
        this.t = NutsOutputFormat.JSON;
        this.ws = ws;
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsArgument n = commandLine.peek();
        if (n != null) {
            boolean enabled = n.isEnabled();
            NutsArgument a;
            if ((a = commandLine.nextString(DefaultPropertiesFormat.OPTION_MULTILINE_PROPERTY)) != null) {
                if (enabled) {
                    NutsArgument i = a.getArgumentValue();
                    extraConfig.add(a.getString());
                    addMultilineProperty(i.getStringKey(), i.getStringValue());
                }
            } else {
                a = commandLine.next();
                if(!a.isOption() || a.isEnabled()) {
                    extraConfig.add(a.getString());
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void print(PrintStream w) {
        ws.json().value(getValue()).print(w);
    }

    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }
}
