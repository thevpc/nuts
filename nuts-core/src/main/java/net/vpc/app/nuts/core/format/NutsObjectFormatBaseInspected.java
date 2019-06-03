/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.NutsXmlUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

/**
 *
 * @author vpc
 */
public class NutsObjectFormatBaseInspected extends NutsObjectFormatBase {

    final NutsOutputFormat t;
    final NutsWorkspace ws;
    final Object data;
    private String rootName = "value";
    private List<String> extraConfig = new ArrayList<>();
    private Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatBaseInspected(NutsOutputFormat t, NutsWorkspace ws, Object data) {
        super(ws, (t == null ? NutsOutputFormat.PLAIN : t).name().toLowerCase() + "-format");
        this.t = t == null ? NutsOutputFormat.PLAIN : t;
        this.ws = ws;
        this.data = data;
    }

    @Override
    public boolean configureFirst(NutsCommand commandLine) {
        NutsArgument n = commandLine.peek();
        if (n != null) {
            NutsArgument a;
            if ((a = commandLine.nextString(DefaultPropertiesFormat.OPTION_MULTILINE_PROPERTY)) != null) {
                NutsArgument i = a.getValue();
                extraConfig.add(a.getString());
                addMultilineProperty(i.getKey().getString(), i.getValue().getString());
            } else {
                extraConfig.add(commandLine.next().getString());
            }
            return true;
        }
        return false;
    }

    @Override
    public void print(Writer w) {
        switch (t) {
            case PLAIN: {
                PrintWriter out = getValidPrintWriter(w);
                NutsPropertiesFormat ff = ws.formatter().createPropertiesFormat().model(toMap());
                ff.configure(ws.parser().parseCommand(extraConfig), true);
                ff.configure(ws.parser().parseCommand("--compact=false"), true);
                ff.print(out);
                break;
            }
            case PROPS: {
                PrintWriter out = getValidPrintWriter(w);
                NutsPropertiesFormat ff = ws.formatter().createPropertiesFormat().model(toMap());
                ff.configure(ws.parser().parseCommand(extraConfig), true);
                ff.configure(ws.parser().parseCommand("--props"), true);
                ff.print(out);
                break;
            }
            case JSON: {
                ws.io().json().write(data, w);
                break;
            }
            case TABLE: {
                NutsTableFormat t = ws.formatter().createTableFormat();
                t.configure(ws.parser().parseCommand(extraConfig), true);
                t.addHeaderCells("Name", "Value");
                for (Map.Entry<String, String> entry : ObjectOutputFormatWriterHelper.indentMap(toMap(), "").entrySet()) {
                    t.newRow();
                    String[] arr = getMultilineArray(stringValue(entry.getKey()), entry.getValue());
                    if (arr == null) {
                        t.addCells(stringValue(entry.getKey()), stringValue(entry.getValue()));
                    } else {
                        t.addCells(stringValue(entry.getKey()), arr[0]);
                        for (int i = 1; i < arr.length; i++) {
                            t.newRow();
                            t.addCells("", arr[i]);
                        }
                    }
                }
                t.print(w);
                break;
            }
            case TREE: {
                NutsTreeFormat t = ws.formatter().createTreeFormat();
                t.configure(ws.parser().parseCommand(extraConfig), true);
                t.setModel(new MyNutsTreeModel(ws, rootName, toMap(),getValidSession()) {
                    @Override
                    protected String[] getMultilineArray(String key, Object value) {
                        return NutsObjectFormatBaseInspected.this.getMultilineArray(key, value);
                    }
                });
                t.print(w);
                break;
            }
            case XML: {
                NutsXmlUtils.print(rootName, new CanonicalBuilder(ws, new NutsFetchDisplayOptions(ws)).toCanonical(data), w, false, null);
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(ws, "Unsupported " + t);
            }
        }
    }

    private Map toMap() {
        CanonicalBuilder c=new CanonicalBuilder(ws, new NutsFetchDisplayOptions(ws));
        return c.toMap(data);
    }

    private String[] getMultilineArray(String key, Object value) {
        String sep = getMultilineSeparator(key);
        if (sep == null) {
            return null;
        }
        String[] vv = stringValue(value).split(sep);
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

    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }
    
     public String stringValue(Object o) {
        return CoreCommonUtils.stringValueFormatted(o, ws, getValidSession());
    }
}
