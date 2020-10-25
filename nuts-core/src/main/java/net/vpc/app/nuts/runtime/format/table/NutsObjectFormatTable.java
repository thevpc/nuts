/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.format.table;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;

import java.io.PrintStream;
import java.util.*;
import net.vpc.app.nuts.runtime.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.runtime.format.NutsObjectFormatBase;

/**
 *
 * @author vpc
 */
public class NutsObjectFormatTable extends NutsObjectFormatBase {

    final NutsOutputFormat t;
    private String rootName = "";
    private List<String> extraConfig = new ArrayList<>();
    private Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatTable(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.TABLE.id() + "-format");
        this.t = NutsOutputFormat.TABLE;
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
                NutsArgument i = a.getArgumentValue();
                if(a.isEnabled()) {
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

    public void print(PrintStream w, NutsElement value) {
        switch (value.type()) {
            case BOOLEAN:
            case DATE:
            case STRING:
            case INTEGER:
            case FLOAT:
            case NULL:{
                List<NutsElement> a = new ArrayList<>();
                a.add(value);
                print(w, getWorkspace().formats().element().toElement(a));
                break;
            }
            case OBJECT: {
                print(w, getWorkspace().formats().element().toElement(value.object().children()));
                break;
            }
            case ARRAY: {
                NutsTableFormat t = getWorkspace().formats().table();
                NutsMutableTableModel model = t.createModel();
                t.setModel(model);
                t.configure(true, getExtraConfigArray());
                LinkedHashSet<String> columns = new LinkedHashSet<>();
                resolveColumns(value, columns);
                for (String column : columns) {
                    model.addHeaderCell(column);
                }
                for (NutsElement elem : value.array().children()) {
                    model.newRow();
                    switch (elem.type()) {
                        case OBJECT: {
                            Map<String, NutsElement> m = new HashMap<>();
                            for (NutsNamedElement vv : elem.object().children()) {
                                m.put(vv.getName(), vv.getValue());
                            }
                            for (String column : columns) {
                                NutsElement vv = m.get(column);
                                if (vv != null) {
                                    model.addCell(formatObject(vv));
                                } else {
                                    model.addCell("");
                                }
                            }
                            break;
                        }
                        default: {
                            for (String column : columns) {
                                if (column.equals("value")) {
                                    model.addCell(formatObject(elem/*.primitive().getValue()*/));
                                } else {
                                    model.addCell("");
                                }
                            }
                        }
                    }
                }
                t.print(w);
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getWorkspace(), "Unsupported " + value.type());
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
        return CoreCommonUtils.stringValueFormatted(any, false, getValidSession());
    }
}
