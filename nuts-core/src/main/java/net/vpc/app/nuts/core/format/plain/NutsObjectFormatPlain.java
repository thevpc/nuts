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
import java.text.SimpleDateFormat;
import java.util.*;
import net.vpc.app.nuts.core.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.core.format.NutsObjectFormatBase;

/**
 *
 * @author vpc
 */
public class NutsObjectFormatPlain extends NutsObjectFormatBase {

    final NutsOutputFormat t;
    final NutsWorkspace ws;
    private String rootName = "";
    private List<String> extraConfig = new ArrayList<>();
    private Map<String, String> multilineProperties = new HashMap<>();

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
            case UNKNWON: {
                out.print(value.toString());
                out.flush();
                break;
            }
            case NULL: {
                break;
            }
            case DATE: {
                out.print(ws.io().getTerminalFormat().escapeText(
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(
                                value.primitive().getDate()
                        )));
                out.flush();
                break;
            }
            case ARRAY: {
                boolean first = true;
                for (NutsElement datum : value.array().children()) {
                    if (first) {
                        first = false;
                    } else {
                        out.println();
                    }
                    print(out, datum);
                }
                break;
            }
            case OBJECT: {
                boolean first = true;
                for (NutsNamedElement datum : value.object().children()) {
                    if (first) {
                        first = false;
                    } else {
                        out.println();
                    }
                    out.printf("%s = ", datum.getName());
                    print(out, datum.getValue());
                }
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
        return CoreCommonUtils.stringValueFormatted(any, false,getValidSession());
    }
}
