///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.runtime.format.json;
//
//import net.thevpc.nuts.NutsArgument;
//import net.thevpc.nuts.NutsCommandLine;
//import net.thevpc.nuts.NutsContentType;
//import net.thevpc.nuts.NutsWorkspace;
//
//import java.io.PrintStream;
//import java.util.*;
//
//import net.thevpc.nuts.runtime.format.NutsObjectFormatBase;
//import net.thevpc.nuts.runtime.format.props.DefaultPropertiesFormat;
//
///**
// * @author vpc
// */
//public class NutsObjectFormatJson extends NutsObjectFormatBase {
//
//    private final NutsContentType t;
//    private final String rootName = "";
//    private final List<String> extraConfig = new ArrayList<>();
//    private final Map<String, String> multilineProperties = new HashMap<>();
//
//    public NutsObjectFormatJson(NutsWorkspace ws) {
//        super(ws, NutsContentType.JSON.id() + "-format");
//        this.t = NutsContentType.JSON;
//    }
//
//    @Override
//    public boolean configureFirst(NutsCommandLine commandLine) {
//        NutsArgument n = commandLine.peek();
//        if (n != null) {
//            NutsArgument a;
//            boolean enabled = n.isEnabled();
//            if ((a = commandLine.nextString(DefaultPropertiesFormat.OPTION_MULTILINE_PROPERTY)) != null) {
//                if (enabled) {
//                    NutsArgument i = a.getArgumentValue();
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
//        getWorkspace().formats().element().setContentType(NutsContentType.JSON).setValue(getValue()).print(w);
//    }
//
//    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
//        multilineProperties.put(property, separator);
//        return this;
//    }
//}
