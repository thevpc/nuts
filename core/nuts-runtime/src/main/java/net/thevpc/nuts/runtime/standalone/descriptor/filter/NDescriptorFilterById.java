///**
// * ====================================================================
// *            Nuts : Network Updatable Things Service
// *                  (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages
// * and libraries for runtime execution. Nuts is the ultimate companion for
// * maven (and other build managers) as it helps installing all package
// * dependencies at runtime. Nuts is not tied to java and is a good choice
// * to share shell scripts and other 'things' . Its based on an extensible
// * architecture to help supporting a large range of sub managers / repositories.
// *
// * <br>
// *
// * Copyright [2020] [thevpc]
// * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
// * you may  not use this file except in compliance with the License. You may obtain
// * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// * either express or implied. See the License for the specific language
// * governing permissions and limitations under the License.
// * <br>
// * ====================================================================
//*/
//package net.thevpc.nuts.runtime.standalone.descriptor.filter;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.util.NSimplifiable;
//import net.thevpc.nuts.util.NFilterOp;
//
//import java.util.Objects;
//
///**
// *
// * @author thevpc
// */
//public class NDescriptorFilterById extends AbstractDescriptorFilter  {
//
//    private NIdFilter id;
//
//    public NDescriptorFilterById(NIdFilter id) {
//        super(NFilterOp.CONVERT);
//        this.id = id;
//    }
//
//    @Override
//    public boolean acceptDescriptor(NDescriptor descriptor) {
//        if (id != null) {
//            return id.acceptId(descriptor.getId());
//        }
//        return true;
//    }
//
//    @Override
//    public NDescriptorFilter simplify() {
//        if (id != null && id instanceof NSimplifiable) {
//            NIdFilter id2 = ((NSimplifiable<NIdFilter>) id).simplify();
//            if (id2 != id) {
//                if (id2 == null) {
//                    return null;
//                }
//                return new NDescriptorFilterById(id2);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public String toString() {
//        return "Id{" + id + '}';
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 97 * hash + Objects.hashCode(this.id);
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final NDescriptorFilterById other = (NDescriptorFilterById) obj;
//        if (!Objects.equals(this.id, other.id)) {
//            return false;
//        }
//        return true;
//    }
//
//}
