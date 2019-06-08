/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.props;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import net.vpc.app.nuts.core.format.NutsObjectFormatBase;

/**
 *
 * @author vpc
 */
public class NutsObjectFormatProps extends NutsObjectFormatBase {

    final NutsOutputFormat t;
    final NutsWorkspace ws;
    private String rootName = "";
    private List<String> extraConfig = new ArrayList<>();
    private Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatProps(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.PROPS.name().toLowerCase() + "-format");
        this.t = NutsOutputFormat.PROPS;
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
        PrintWriter out = getValidPrintWriter(w);
        NutsPropertiesFormat ff = ws.format().props().model(toMap());
        ff.configure(true, getExtraConfigArray());
        ff.configure(true, "--props");
        ff.print(out);
    }

    private String[] getExtraConfigArray() {
        return extraConfig.toArray(new String[0]);
    }

    private Map toMap() {
        switch (getValue().type()) {
            case BOOLEAN:
            case DATE:
            case NUMBER:
            case STRING:
            case UNKNWON: {
                LinkedHashMap<String, Object> a = new LinkedHashMap<>();
                a.put("value", CoreCommonUtils.stringValueFormatted(getValue().primitive().getValue(), ws, getValidSession()));
                return a;

            }
            case ARRAY: {
                LinkedHashMap<String, Object> a = new LinkedHashMap<>();
                int index = 1;
                for (NutsElement datum : getValue().array().children()) {
                    a.put(String.valueOf(index), CoreCommonUtils.stringValueFormatted(datum, ws, getValidSession()));
                    index++;
                }
                return a;
            }
            case OBJECT: {
                LinkedHashMap<String, Object> a = new LinkedHashMap<>();
                for (NutsNamedElement datum : getValue().object().children()) {
                    a.put(
                            datum.getName(),
                            CoreCommonUtils.stringValueFormatted(datum.getValue(), ws, getValidSession())
                    );
                }
                return a;
            }
        }
        throw new NutsUnsupportedArgumentException(ws, getValue().type().name());
    }

    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }
}
