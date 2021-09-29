///**
// * ====================================================================
// *            Nuts : Network Updatable Things Service
// *                  (universal package manager)
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
//package net.thevpc.nuts.runtime.core.format.elem;
//
//import net.thevpc.nuts.NutsElement;
//import net.thevpc.nuts.NutsElementEntry;
//import net.thevpc.nuts.NutsElementEntryBuilder;
//import net.thevpc.nuts.NutsElementFormat;
//import net.thevpc.nuts.NutsPrimitiveElementBuilder;
//import net.thevpc.nuts.NutsSession;
//
///**
// *
// * @author vpc
// */
//public class DefaultNutsElementEntryBuilder extends AbstractNutsElementBaseBuilder implements NutsElementEntryBuilder {
//
//    private NutsElement key;
//    private NutsElement value;
//    private NutsSession session;
//
//    public DefaultNutsElementEntryBuilder(NutsSession session) {
//        this.session = session;
//    }
//
//    public NutsElement getKey() {
//        return key;
//    }
//
//    public NutsElementEntryBuilder setKey(NutsElement key) {
//        this.key = key;
//        return this;
//    }
//
//    @Override
//    public NutsElement getValue() {
//        return value;
//    }
//
//    @Override
//    public NutsElementEntryBuilder setValue(NutsElement value) {
//        this.value = value;
//        return this;
//    }
//
//    @Override
//    public NutsElementEntryBuilder set(NutsElement key, NutsElement value) {
//        setKey(key);
//        setValue(value);
//        return this;
//    }
//    
//
//    @Override
//    public NutsElementEntry build() {
//        return new DefaultNutsElementEntry(
//                key == null ? _element().forNull() : key,
//                value == null ? _element().forNull() : value
//        );
//    }
//
//    private NutsElementFormat _element() {
//        return session.elem();
//    }
//
//}
