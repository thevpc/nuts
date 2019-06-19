/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.plain;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import net.vpc.app.nuts.core.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.core.format.NutsObjectFormatBase;
import net.vpc.app.nuts.core.format.table.NutsObjectFormatTable;
import net.vpc.app.nuts.core.format.tree.NutsObjectFormatTree;

/**
 *
 * @author vpc
 */
public class NutsObjectFormatPlain extends NutsObjectFormatBase {

    private final NutsOutputFormat t;
    private final NutsWorkspace ws;
    private final String rootName = "";
    private final List<String> extraConfig = new ArrayList<>();
    private final Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatPlain(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.PLAIN.name().toLowerCase() + "-format");
        this.t = NutsOutputFormat.PLAIN;
        this.ws = ws;
    }

    @Override
    public NutsObjectFormat setValue(Object value) {
        return super.setValue(ws.format().element().toElement(value));
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
                extraConfig.add(a.getString());
                addMultilineProperty(i.getStringKey(), i.getStringValue());
            } else {
                extraConfig.add(commandLine.next().getString());
            }
            return true;
        }
        return false;
    }

    private String getFormattedPrimitiveValue(NutsElement value) {
        switch (value.type()) {
            default: {
                throw new NutsUnsupportedArgumentException(ws, value.type().toString());
            }
        }
    }

    @Override
    public void print(Writer w) {
        print(w, getValue());
    }

    public void print(Writer w, NutsElement value) {
        PrintWriter out = getValidPrintWriter(w);
        switch (value.type()) {
            case STRING: {
                out.print(value.primitive().getString());
                out.flush();
                break;
            }
            case BOOLEAN: {
                out.print(value.primitive().getBoolean());
                out.flush();
                break;
            }
            case NUMBER: {
                out.print(value.primitive().getNumber());
                out.flush();
                break;
            }
            case DATE: {
                out.print(ws.io().getTerminalFormat().escapeText(value.primitive().getDate().toString()));
                out.flush();
                break;
            }
            case UNKNWON: {
                out.print(value.toString());
                out.flush();
                break;
            }
            case NULL: {
                break;
            }
            case ARRAY: {
                NutsObjectFormatTable table = new NutsObjectFormatTable(ws);
                table.configure(true, "--border=spaces");
                table.set(value).print(w);
                break;
            }
            case OBJECT: {
                NutsObjectFormatTree tree = new NutsObjectFormatTree(ws);
                tree.set(value).print(w);
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(ws, value.type().toString());
            }
        }
    }

    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    private String formatObject(Object any) {
        return CoreCommonUtils.stringValueFormatted(any, false, getValidSession());
    }
}
