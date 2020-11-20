///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.runtime.format.props;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.format.NutsObjectFormatBase;
//import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
//import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;
//
//import java.io.PrintStream;
//import java.util.*;
//
///**
// *
// * @author vpc
// */
//public class NutsObjectFormatProps extends NutsObjectFormatBase {
//
//    private final String rootName = "";
//    private final boolean omitNull = true;
//    private final boolean escapeText = false;
//    private final List<String> extraConfig = new ArrayList<>();
//    private final Map<String, String> multilineProperties = new HashMap<>();
//
//    public NutsObjectFormatProps(NutsWorkspace workspace) {
//        super(workspace, NutsContentType.PROPS.id() + "-format");
//    }
//
//    @Override
//    public NutsObjectFormat setValue(Object value) {
//        return super.setValue(getWorkspace().formats().element().convert(value,NutsElement.class));
//    }
//
//    @Override
//    public NutsElement getValue() {
//        return (NutsElement) super.getValue();
//    }
//
//    @Override
//    public boolean configureFirst(NutsCommandLine commandLine) {
//        NutsArgument n = commandLine.peek();
//        if (n != null) {
//            NutsArgument a;
//            if ((a = commandLine.nextString(DefaultPropertiesFormat.OPTION_MULTILINE_PROPERTY)) != null) {
//                NutsArgument i = a.getArgumentValue();
//                if(a.isEnabled()) {
//                    extraConfig.add(a.getString());
//                    addMultilineProperty(i.getStringKey(), i.getStringValue());
//                }
//            } else {
//                a = commandLine.next();
//                if(!a.isOption() || a.isEnabled()) {
//                    extraConfig.add(a.getString());
//                }
//            }
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void print(PrintStream w) {
//        PrintStream out = getValidPrintStream(w);
//        NutsPropertiesFormat ff = getWorkspace().formats().props().model(toMap());
//        ff.configure(true, getExtraConfigArray());
//        ff.configure(true, "--escape-text=false");
//        ff.print(out);
//    }
//
//    private String[] getExtraConfigArray() {
//        return extraConfig.toArray(new String[0]);
//    }
//
//    private Map toMap() {
//        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
//        fillMap(getValue(), map, rootName);
//        return map;
//    }
//
//    private void fillMap(NutsElement e, Map<String, String> map, String prefix) {
//        switch (e.type()) {
//            case NULL: {
//                if (omitNull) {
//                    //do nothing;
//                } else {
//                    String k = (CoreStringUtils.isBlank(prefix)) ? "value" : prefix;
//                    map.put(k, stringValue(e.primitive().getValue()));
//                }
//                break;
//            }
//            case BOOLEAN:
//            case DATE:
//            case INTEGER:
//            case FLOAT:
//            case STRING: {
//                String k = (CoreStringUtils.isBlank(prefix)) ? "value" : prefix;
//                map.put(k, stringValue(e.primitive().getValue()));
//                break;
//            }
//            case ARRAY: {
//                int index = 1;
//                for (NutsElement datum : e.array().children()) {
//                    String k = (CoreStringUtils.isBlank(prefix)) ? String.valueOf(index) : (prefix + "." + String.valueOf(index));
//                    fillMap(datum, map, k);
//                    index++;
//                }
//                break;
//            }
//            case OBJECT: {
//                for (NutsNamedElement datum : e.object().children()) {
//                    String k = (CoreStringUtils.isBlank(prefix)) ? datum.getName() : (prefix + "." + datum.getName());
//                    fillMap(datum.getValue(), map, k);
//                }
//                break;
//            }
//            default: {
//                throw new NutsUnsupportedArgumentException(getWorkspace(), e.type().name());
//            }
//        }
//    }
//
//    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
//        multilineProperties.put(property, separator);
//        return this;
//    }
//
//    public String stringValue(Object o) {
//        return CoreCommonUtils.stringValueFormatted(o, escapeText, getValidSession());
//    }
//}
