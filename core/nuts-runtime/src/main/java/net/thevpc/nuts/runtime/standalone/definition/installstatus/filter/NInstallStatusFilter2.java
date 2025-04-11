///**
// * ====================================================================
// * Nuts : Network Updatable Things Service
// * (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages
// * and libraries for runtime execution. Nuts is the ultimate companion for
// * maven (and other build managers) as it helps installing all package
// * dependencies at runtime. Nuts is not tied to java and is a good choice
// * to share shell scripts and other 'things' . Its based on an extensible
// * architecture to help supporting a large range of sub managers / repositories.
// *
// * <br>
// * <p>
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
// */
//package net.thevpc.nuts.runtime.standalone.definition.installstatus.filter;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.spi.base.AbstractInstallStatusFilter;
//import net.thevpc.nuts.util.NFilter;
//import net.thevpc.nuts.util.NFilterOp;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Objects;
//
///**
// * Package installation status.
// */
//public class NInstallStatusFilter2 extends AbstractInstallStatusFilter {
//
//    private final int installed;
//    private final int required;
//    private final int obsolete;
//    private final int defaultVersion;
//    private final int deployed;
//
//
//    public NInstallStatusFilter2(int installed, int required, int obsolete, int defaultVersion,int deployed) {
//        super(NFilterOp.CUSTOM);
//        this.installed = installed;
//        this.required = required;
//        this.obsolete = obsolete;
//        this.defaultVersion = defaultVersion;
//        this.deployed = deployed;
//    }
//
//    public boolean isInstalled() {
//        return installed == 1;
//    }
//
//    public boolean isRequired() {
//        return required == 1;
//    }
//
//    public boolean isObsolete() {
//        return obsolete == 1;
//    }
//
//    public boolean isDefaultVersion() {
//        return defaultVersion == 1;
//    }
//
//    public boolean isNotInstalled() {
//        return installed == -1;
//    }
//
//    public boolean isNotRequired() {
//        return required == -1;
//    }
//
//    public boolean isNotObsolete() {
//        return obsolete == -1;
//    }
//
//    public boolean isNotDefaultVersion() {
//        return defaultVersion == -1;
//    }
//
//    public Boolean getInstalled() {
//        return installed == 1 ? Boolean.TRUE : installed == -1 ? Boolean.FALSE : null;
//    }
//
//    public Boolean getRequired() {
//        return required == 1 ? Boolean.TRUE : required == -1 ? Boolean.FALSE : null;
//    }
//
//    public Boolean getObsolete() {
//        return obsolete == 1 ? Boolean.TRUE : obsolete == -1 ? Boolean.FALSE : null;
//    }
//
//    public Boolean getDefaultVersion() {
//        return defaultVersion == 1 ? Boolean.TRUE : defaultVersion == -1 ? Boolean.FALSE : null;
//    }
//
//    @Override
//    public boolean acceptInstallStatus(NInstallStatus status) {
//        if (status == null) {
//            return false;
//        }
//        Boolean i = getInstalled();
//        if (i != null) {
//            if (i != status.isInstalled()) {
//                return false;
//            }
//        }
//        i = getRequired();
//        if (i != null) {
//            if (i != status.isRequired()) {
//                return false;
//            }
//        }
//        i = getObsolete();
//        if (i != null) {
//            if (i != status.isObsolete()) {
//                return false;
//            }
//        }
//        i = getDefaultVersion();
//        if (i != null) {
//            if (i != status.isDefaultVersion()) {
//                return false;
//            }
//        }
//        if(deployed>0){
//            return status.isDeployed();
//        }else if (deployed<0){
//            return !status.isDeployed();
//        }
//        return true;
//    }
//
//    @Override
//    public NInstallStatusFilter simplify() {
//        return this;
//    }
//
//
//
//
//    @Override
//    public List<NFilter> getSubFilters() {
//        return Collections.emptyList();
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(installed, required, obsolete, defaultVersion);
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        NInstallStatusFilter2 that = (NInstallStatusFilter2) o;
//        return installed == that.installed && required == that.required && obsolete == that.obsolete && defaultVersion == that.defaultVersion
//                ;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        boolean addPars=false;
//        if (installed > 0) {
//            sb.append("installed");
//        } else if (installed < 0) {
//            sb.append("!installed");
//        }
//        if (required > 0) {
//            if (sb.length() > 0) {
//                sb.append("&");
//                addPars=true;
//            }
//            sb.append("required");
//        }else if (required < 0) {
//            addPars=true;
//            if (sb.length() > 0) {
//                sb.append("&");
//            }
//            sb.append("!required");
//        }
//        if (defaultVersion > 0) {
//            if (sb.length() > 0) {
//                addPars=true;
//                sb.append("&");
//            }
//            sb.append("defaultVersion");
//        }else if (defaultVersion < 0) {
//            addPars=true;
//            if (sb.length() > 0) {
//                sb.append("&");
//            }
//            sb.append("!defaultVersion");
//        }
//        if (obsolete > 0) {
//            if (sb.length() > 0) {
//                addPars=true;
//                sb.append("&");
//            }
//            sb.append("obsolete");
//        } else if (obsolete < 0) {
//            addPars=true;
//            if (sb.length() > 0) {
//                sb.append("&");
//            }
//            sb.append("!obsolete");
//        }
//        if (sb.length() == 0) {
//            addPars=true;
//            sb.append("!deployed");
//        }
//        if(addPars){
//            sb.insert(0,'(');
//            sb.append(')');
//        }
//        return sb.toString();
//    }
//}
