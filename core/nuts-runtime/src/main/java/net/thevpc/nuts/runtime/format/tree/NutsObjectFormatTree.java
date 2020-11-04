/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.format.tree;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;

import java.io.PrintStream;
import java.util.*;
import net.thevpc.nuts.runtime.format.props.DefaultPropertiesFormat;
import net.thevpc.nuts.runtime.format.NutsObjectFormatBase;

/**
 *
 * @author vpc
 */
public class NutsObjectFormatTree extends NutsObjectFormatBase {

    final NutsOutputFormat t;
    private String rootName = "";
    private List<String> extraConfig = new ArrayList<>();
    private Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatTree(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.TREE.id() + "-format");
        this.t = NutsOutputFormat.TREE;
    }

    @Override
    public NutsObjectFormat setValue(Object value) {
        return super.setValue(getWorkspace().formats().element().toElement(value));
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
                if(a.isEnabled()) {
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
        NutsTreeFormat t = getWorkspace().formats().tree();
        t.configure(true, getExtraConfigArray());
        t.setModel(new NutsElementTreeModel(getWorkspace(), rootName, getValue(), getValidSession()) {
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
