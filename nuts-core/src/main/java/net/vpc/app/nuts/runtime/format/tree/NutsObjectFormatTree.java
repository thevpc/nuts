/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.format.tree;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;

import java.io.PrintStream;
import java.io.Writer;
import java.util.*;
import net.vpc.app.nuts.runtime.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.runtime.format.NutsObjectFormatBase;

/**
 *
 * @author vpc
 */
public class NutsObjectFormatTree extends NutsObjectFormatBase {

    final NutsOutputFormat t;
    final NutsWorkspace ws;
    private String rootName = "";
    private List<String> extraConfig = new ArrayList<>();
    private Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatTree(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.TREE.id() + "-format");
        this.t = NutsOutputFormat.TREE;
        this.ws = ws;
    }

    @Override
    public NutsObjectFormat setValue(Object value) {
        return super.setValue(ws.element().toElement(value));
    }

    @Override
    public NutsElement getValue() {
        return (NutsElement) super.getValue();
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsArgument n = commandLine.peek();
        if (n != null) {
            NutsArgument a;
            if ((a = commandLine.nextString(DefaultPropertiesFormat.OPTION_MULTILINE_PROPERTY)) != null) {
                NutsArgument i = a.getArgumentValue();
                if(a.isEnabled()) {
                    extraConfig.add(a.getString());
                    addMultilineProperty(i.getStringKey(), i.getStringValue());
                }
            } else {
                a = commandLine.next();
                if(!a.isOption() || a.isEnabled()) {
                    extraConfig.add(commandLine.next().getString());
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void print(PrintStream w) {
        NutsTreeFormat t = ws.tree();
        t.configure(true, getExtraConfigArray());
        t.setModel(new NutsElementTreeModel(ws, rootName, getValue(), getValidSession()) {
            @Override
            protected String[] getMultilineArray(String key, NutsElement value) {
                return NutsObjectFormatTree.this.getMultilineArray(key, value);
            }
        });
        t.print(w);
    }

    private String[] getMultilineArray(String key, Object value) {
        String sep = getMultilineSeparator(key);
        if (sep == null) {
            return null;
        }
        String[] vv = CoreCommonUtils.stringValue(value).split(sep);
        if (vv.length == 0 || vv.length == 1) {
            return null;
        }
        return vv;
    }

    private String getMultilineSeparator(String key) {
        String sep = multilineProperties.get(key);
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

    private String[] getExtraConfigArray() {
        return extraConfig.toArray(new String[0]);
    }

    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    private String formatObject(Object any) {
        return CoreCommonUtils.stringValueFormatted(any, false,getValidSession());
    }
}
