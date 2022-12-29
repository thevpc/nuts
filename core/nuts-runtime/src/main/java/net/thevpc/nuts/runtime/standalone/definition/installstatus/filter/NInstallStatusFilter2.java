/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.definition.installstatus.filter;

import net.thevpc.nuts.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Package installation status.
 */
public class NInstallStatusFilter2 extends AbstractInstallStatusFilter {

    private final int installed;
    private final int required;
    private final int obsolete;
    private final int defaultVersion;
//    private static final NutsInstallStatusFilter2[] ALL=new NutsInstallStatusFilter2[256];
//    static {
//        for (int i = 0; i < 256; i++) {
//            ALL[i]=new NutsInstallStatusFilter2(
//                    (i&0x1)!=0?1:(i&0x2)!=0?-1:0,
//                    (i&0x4)!=0?1:(i&0x8)!=0?-1:0,
//                    (i&0x10)!=0?1:(i&0x20)!=0?-1:0,
//                    (i&0x40)!=0?1:(i&0x80)!=0?-1:0
//            );
//        }
//    }


//    public static NutsInstallStatusFilter2 of(NutsWorkspace ws, Boolean installed, Boolean required, Boolean obsolete, Boolean defaultVersion) {
//        return ALL[
//                ((installed!=null && installed)?0x1:0)+
//                ((installed!=null && !installed)?0x2:0)+
//                ((required!=null && required)?0x4:0)+
//                ((required!=null && !required)?0x8:0)+
//                ((obsolete!=null && obsolete)?0x10:0)+
//                ((obsolete!=null && !obsolete)?0x20:0)+
//                ((defaultVersion!=null && defaultVersion)?0x40:0)+
//                ((defaultVersion!=null && !defaultVersion)?0x80:0)
//                ];
//    }


    public NInstallStatusFilter2(NSession session, int installed, int required, int obsolete, int defaultVersion) {
        super(session, NFilterOp.CUSTOM);
        this.installed = installed;
        this.required = required;
        this.obsolete = obsolete;
        this.defaultVersion = defaultVersion;
    }

    public boolean isInstalled() {
        return installed == 1;
    }

    public boolean isRequired() {
        return required == 1;
    }

    public boolean isObsolete() {
        return obsolete == 1;
    }

    public boolean isDefaultVersion() {
        return defaultVersion == 1;
    }

    public boolean isNotInstalled() {
        return installed == -1;
    }

    public boolean isNotRequired() {
        return required == -1;
    }

    public boolean isNotObsolete() {
        return obsolete == -1;
    }

    public boolean isNotDefaultVersion() {
        return defaultVersion == -1;
    }

    public Boolean getInstalled() {
        return installed == 1 ? Boolean.TRUE : installed == -1 ? Boolean.FALSE : null;
    }

    public Boolean getRequired() {
        return required == 1 ? Boolean.TRUE : required == -1 ? Boolean.FALSE : null;
    }

    public Boolean getObsolete() {
        return obsolete == 1 ? Boolean.TRUE : obsolete == -1 ? Boolean.FALSE : null;
    }

    public Boolean getDefaultVersion() {
        return defaultVersion == 1 ? Boolean.TRUE : defaultVersion == -1 ? Boolean.FALSE : null;
    }

    @Override
    public boolean acceptInstallStatus(NInstallStatus status, NSession session) {
        if (status == null) {
            return false;
        }
        Boolean i = getInstalled();
        if (i != null) {
            if (i != status.isInstalled()) {
                return false;
            }
        }
        i = getRequired();
        if (i != null) {
            if (i != status.isRequired()) {
                return false;
            }
        }
        i = getObsolete();
        if (i != null) {
            if (i != status.isObsolete()) {
                return false;
            }
        }
        i = getDefaultVersion();
        if (i != null) {
            if (i != status.isDefaultVersion()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NInstallStatusFilter simplify() {
        return this;
    }


   

    @Override
    public List<NFilter> getSubFilters() {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        return Objects.hash(installed, required, obsolete, defaultVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NInstallStatusFilter2 that = (NInstallStatusFilter2) o;
        return installed == that.installed && required == that.required && obsolete == that.obsolete && defaultVersion == that.defaultVersion 
                ;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean addPars=false;
        if (installed > 0) {
            sb.append("installed");
        } else if (installed < 0) {
            sb.append("!installed");
        }
        if (required > 0) {
            if (sb.length() > 0) {
                sb.append("&");
                addPars=true;
            }
            sb.append("required");
        }else if (required < 0) {
            addPars=true;
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append("!required");
        }
        if (defaultVersion > 0) {
            if (sb.length() > 0) {
                addPars=true;
                sb.append("&");
            }
            sb.append("defaultVersion");
        }else if (defaultVersion < 0) {
            addPars=true;
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append("!defaultVersion");
        }
        if (obsolete > 0) {
            if (sb.length() > 0) {
                addPars=true;
                sb.append("&");
            }
            sb.append("obsolete");
        } else if (obsolete < 0) {
            addPars=true;
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append("!obsolete");
        }
        if (sb.length() == 0) {
            addPars=true;
            sb.append("!deployed");
        }
        if(addPars){
            sb.insert(0,'(');
            sb.append(')');
        }
        return sb.toString();
    }
}
