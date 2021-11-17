///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.runtime.deprecated;
//
//import net.thevpc.nuts.NutsBlankable;
//import net.thevpc.nuts.NutsEnum;
//import net.thevpc.nuts.NutsParseEnumException;
//import net.thevpc.nuts.NutsSession;
//
///**
// *
// * @author thevpc
// */
//public enum RemoteRepoApi  implements NutsEnum {
//
//    DEFAULT,
//    MAVEN,
//    GITHUB,
//    DOTFILEFS,
//    HTMLFS,
//    UNSUPPORTED;
//    private String id;
//
//    RemoteRepoApi() {
//        this.id = name().toLowerCase().replace('_', '-');
//    }
//
//    @Override
//    public String id() {
//        return id;
//    }
//
//    public static RemoteRepoApi parseLenient(String value) {
//        return parseLenient(value, null);
//    }
//
//    public static RemoteRepoApi parseLenient(String value, RemoteRepoApi emptyOrErrorValue) {
//        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
//    }
//
//    public static RemoteRepoApi parseLenient(String value, RemoteRepoApi emptyValue, RemoteRepoApi errorValue) {
//        if (value == null) {
//            value = "";
//        } else {
//            value = value.toUpperCase().trim().replace('-', '_');
//        }
//        if (value.isEmpty()) {
//            return emptyValue;
//        }
//        try {
//            return RemoteRepoApi.valueOf(value.toUpperCase());
//        } catch (Exception notFound) {
//            return errorValue;
//        }
//    }
//
//    public static RemoteRepoApi parse(String value, NutsSession session) {
//        return parse(value, null,session);
//    }
//
//    public static RemoteRepoApi parse(String value, RemoteRepoApi emptyValue, NutsSession session) {
//        RemoteRepoApi v = parseLenient(value, emptyValue, null);
//        if(v==null){
//            if(!NutsBlankable.isBlank(value)){
//                throw new NutsParseEnumException(session,value,RemoteRepoApi.class);
//            }
//        }
//        return v;
//    }
//}
