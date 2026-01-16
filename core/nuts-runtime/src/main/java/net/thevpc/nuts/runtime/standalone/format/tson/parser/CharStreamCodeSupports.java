//package net.thevpc.nuts.runtime.standalone.format.tson.parser;
//
//import net.thevpc.nuts.runtime.standalone.format.tson.CharStreamCodeSupport;
//
//public class CharStreamCodeSupports {
//    public static CharStreamCodeSupport of(String language) {
//        if (language == null || language.isEmpty()) {
//            return new CharStreamCodeSupportDefault();
//        }
//        switch (language) {
//            case "java": {
//                return new CharStreamCodeSupportJava();
//            }
//        }
//        throw new IllegalArgumentException("Unsupported Language " + language);
//    }
//}
