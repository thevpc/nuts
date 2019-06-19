/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.table;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

import java.io.Writer;
import java.util.*;
import net.vpc.app.nuts.core.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.core.format.NutsObjectFormatBase;

/**
 *
 * @author vpc
 */
public class NutsObjectFormatTable extends NutsObjectFormatBase {

    final NutsOutputFormat t;
    final NutsWorkspace ws;
    private String rootName = "";
    private List<String> extraConfig = new ArrayList<>();
    private Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatTable(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.TABLE.name().toLowerCase() + "-format");
        this.t = NutsOutputFormat.TABLE;
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

    @Override
    public void print(Writer w) {
        print(w, getValue());
    }

    public void resolveColumns(NutsElement value, LinkedHashSet<String> columns) {
        switch (value.type()) {
            case OBJECT: {
                for (NutsNamedElement nutsNamedValue : value.object().children()) {
                    columns.add(nutsNamedValue.getName());
                }
                break;
            }
            case ARRAY: {
                for (NutsElement value2 : value.array().children()) {
                    resolveColumns(value2, columns);
                }
                break;
            }
            default: {
                columns.add("value");
            }
        }
    }

    public void print(Writer w, NutsElement value) {
        switch (value.type()) {
            case BOOLEAN:
            case DATE:
            case STRING:
            case NUMBER:
            case NULL:
            case UNKNWON: {
                List<NutsElement> a = new ArrayList<>();
                a.add(value);
                print(w, ws.format().element().toElement(a));
                break;
            }
            case OBJECT: {
                print(w, ws.format().element().toElement(value.object().children()));
                break;
            }
            case ARRAY: {
                NutsTableFormat t = ws.format().table();
                t.configure(true, getExtraConfigArray());
                LinkedHashSet<String> columns = new LinkedHashSet<>();
                resolveColumns(value, columns);
                for (String column : columns) {
                    t.addHeaderCell(column);
                }
                for (NutsElement elem : value.array().children()) {
                    t.newRow();
                    switch (elem.type()) {
                        case OBJECT: {
                            Map<String, NutsElement> m = new HashMap<>();
                            for (NutsNamedElement vv : elem.object().children()) {
                                m.put(vv.getName(), vv.getValue());
                            }
                            for (String column : columns) {
                                NutsElement vv = m.get(column);
                                if (vv != null) {
                                    t.addCell(formatObject(vv));
                                } else {
                                    t.addCell("");
                                }
                            }
                            break;
                        }
                        default: {
                            for (String column : columns) {
                                if (column.equals("value")) {
                                    t.addCell(formatObject(elem.primitive().getValue()));
                                } else {
                                    t.addCell("");
                                }
                            }
                        }
                    }
                }
                t.print(w);
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(ws, "Unsupported " + value.type());
            }
        }
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
