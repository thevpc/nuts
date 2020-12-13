///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.toolbox.nadmin.config;
//
//import net.thevpc.nuts.NutsApplicationContext;
//import net.thevpc.nuts.NutsWorkspaceExtensionManager;
//
//import java.io.PrintStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import net.thevpc.nuts.NutsCommandLine;
//
///**
// * @author thevpc
// */
//public class ExtensionNAdminSubCommand extends AbstractNAdminSubCommand {
//
//    @Override
//    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {
//        if (cmdLine.next("list extension points", "lxp") != null) {
//            PrintStream out = context.getSession().out();
//            if (cmdLine.isExecMode()) {
//                List<ExtensionPointInfo> all=new ArrayList<>();
//                NutsWorkspaceExtensionManager extensions = context.getWorkspace().extensions();
//                for (Class extension : extensions.getExtensionPoints(context.getSession())) {
//                    ExtensionPointInfo a=new ExtensionPointInfo();
//                    a.name=extension.getName();
//                    a.types= extensions.getExtensionTypes(extension, context.getSession())
//                            .stream().map(Class::getName).toArray(String[]::new);
//                    a.objects= extensions.getExtensionObjects(extension, context.getSession())
//                            .stream().map(x->x==null?"null":(x.getClass().getName()+"::"+x.toString()))
//                                    .toArray(String[]::new);
//                    all.add(a);
//                }
//                context.getSession().formatObject(all).println();
//            }
//            return true;
//        }
//        return false;
//    }
//    public static class ExtensionPointInfo{
//        String name;
//        String[] types;
//        String[] objects;
//    }
//}
