//package net.thevpc.nuts.runtime.optional.gson;
//
//import net.thevpc.nuts.runtime.core.format.elem.NutsElementFactoryContext;
//import net.thevpc.nuts.runtime.core.format.elem.NutsElementStreamFormat;
//
///**
// * ==================================================================== Nuts :
// * Network Updatable Things Service (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages and libraries
// * for runtime execution. Nuts is the ultimate companion for maven (and other
// * build managers) as it helps installing all package dependencies at runtime.
// * Nuts is not tied to java and is a good choice to share shell scripts and
// * other 'things' . Its based on an extensible architecture to help supporting a
// * large range of sub managers / repositories.
// * <br>
// *
// * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
// * or agreed to in writing, software distributed under the License is
// * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the specific language
// * governing permissions and limitations under the License.
// * <br> ====================================================================
// */
///**
// *
// * @author vpc
// */
//public class OptionalGson {
//
//    public static boolean isAvailable() {
//        try {
//            Class.forName("com.google.gson.Gson");
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public static NutsElementStreamFormat createElementStreamFormat(NutsElementFactoryContext context) {
//        if (isAvailable()) {
//            return new GsonItemSerializeManager();
//        }
//        return null;
//    }
//}
