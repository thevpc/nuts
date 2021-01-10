/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
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
package net.thevpc.nuts;

import java.util.function.Predicate;

/**
 * Package installation status.
 * Possible combinations are :
 * <ul>
 *    <li>NOT_INSTALLED</li>
 *    <li>REQUIRED</li>
 *    <li>INSTALLED</li>
 *    <li>INSTALLED REQUIRED</li>
 *    <li>REQUIRED OBSOLETE</li>
 *    <li>INSTALLED OBSOLETE</li>
 *    <li>INSTALLED REQUIRED OBSOLETE</li>
 * </ul>
 * @category Base
 */
public class NutsInstallStatusFilter extends NutsPredicates.BasePredicate<NutsInstallStatus> {

    private final int installed;
    private final  int required;
    private final  int obsolete;
    private final  int defaultVersion;
    private static final NutsInstallStatusFilter[] ALL=new NutsInstallStatusFilter[256];
    static {
        for (int i = 0; i < 256; i++) {
            ALL[i]=new NutsInstallStatusFilter(
                    (i&0x1)!=0?1:(i&0x2)!=0?-1:0,
                    (i&0x4)!=0?1:(i&0x8)!=0?-1:0,
                    (i&0x10)!=0?1:(i&0x20)!=0?-1:0,
                    (i&0x40)!=0?1:(i&0x80)!=0?-1:0
            );
        }
    }

    public static final NutsInstallStatusFilter ANY=of(null, null, null, null);

    public static final NutsInstallStatusFilter INSTALLED=of(true, null, null, null);
    public static final NutsInstallStatusFilter NOT_INSTALLED=of(false, null, null, null);
    public static final NutsInstallStatusFilter REQUIRED=of(null, true, null, null);
    public static final NutsInstallStatusFilter NOT_REQUIRED=of(null, false, null, null);
    public static final NutsInstallStatusFilter OBSOLETE=of(null, null, true, null);
    public static final NutsInstallStatusFilter NOT_OBSOLETE=of(null, null, false, null);

    public static final NutsInstallStatusFilter DEFAULT_VALUE=of(null, null, null, true);
    public static final NutsInstallStatusFilter NOT_DEFAULT_VALUE=of(null, null, null, false);

    public static final Predicate<NutsInstallStatus> DEPLOYED =INSTALLED.or(REQUIRED);

    public static final Predicate<NutsInstallStatus> NOT_DEPLOYED =DEPLOYED.negate();

    public static NutsInstallStatusFilter of(Boolean installed, Boolean required, Boolean obsolete, Boolean defaultVersion) {
        return ALL[
                ((installed!=null && installed)?0x1:0)+
                ((installed!=null && !installed)?0x2:0)+
                ((required!=null && required)?0x4:0)+
                ((required!=null && !required)?0x8:0)+
                ((obsolete!=null && obsolete)?0x10:0)+
                ((obsolete!=null && !obsolete)?0x20:0)+
                ((defaultVersion!=null && defaultVersion)?0x40:0)+
                ((defaultVersion!=null && !defaultVersion)?0x80:0)
                ];
    }


    private NutsInstallStatusFilter(int installed, int required, int obsolete, int defaultVersion) {
        this.installed = installed;
        this.required = required;
        this.obsolete = obsolete;
        this.defaultVersion = defaultVersion;
    }

    public boolean isInstalled() {
        return installed==1;
    }

    public boolean isRequired() {
        return required==1;
    }

    public boolean isObsolete() {
        return obsolete==1;
    }

    public boolean isDefaultVersion() {
        return defaultVersion==1;
    }

    public boolean isNotInstalled() {
        return installed==-1;
    }

    public boolean isNotRequired() {
        return required==-1;
    }

    public boolean isNotObsolete() {
        return obsolete==-1;
    }

    public boolean isNotDefaultVersion() {
        return defaultVersion==-1;
    }

    public Boolean getInstalled() {
        return installed==1?Boolean.TRUE : installed==-1?Boolean.FALSE : null;
    }

    public Boolean getRequired() {
        return required==1?Boolean.TRUE : required==-1?Boolean.FALSE : null;
    }

    public Boolean getObsolete() {
        return obsolete==1?Boolean.TRUE : obsolete==-1?Boolean.FALSE : null;
    }

    public Boolean getDefaultVersion() {
        return defaultVersion==1?Boolean.TRUE : defaultVersion==-1?Boolean.FALSE : null;
    }

    public NutsInstallStatusFilter withInstalled(Boolean installed) {
        return of(installed, getRequired(), getObsolete(), getDefaultVersion());
    }

    public NutsInstallStatusFilter withRequired(Boolean required) {
        return of(getInstalled(), required, getObsolete(), getDefaultVersion());
    }

    public NutsInstallStatusFilter withObsolete(Boolean obsolete) {
        return of(getInstalled(), getRequired(), obsolete, getDefaultVersion());
    }

    public NutsInstallStatusFilter withDefaultVersion(Boolean defaultVersion) {
        return of(getInstalled(), getRequired(), getObsolete(), defaultVersion);
    }

    public NutsInstallStatusFilter withOther(NutsInstallStatusFilter other) {
        return of(
                other.getInstalled()!=null?other.getInstalled():this.getInstalled(),
                other.getRequired()!=null?other.getRequired():this.getRequired(),
                other.getObsolete()!=null?other.getObsolete():this.getObsolete(),
                other.getDefaultVersion()!=null?other.getDefaultVersion():this.getDefaultVersion()
        );
    }



    @Override
    public boolean test(NutsInstallStatus status) {
        return accept(status);
    }

    public boolean accept(NutsInstallStatus status){
        if(status==null){
            return false;
        }
        Boolean i = getInstalled();
        if(i!=null){
            if(i!=status.isInstalled()){
                return false;
            }
        }
        i = getRequired();
        if(i!=null){
            if(i!=status.isRequired()){
                return false;
            }
        }
        i = getObsolete();
        if(i!=null){
            if(i!=status.isObsolete()){
                return false;
            }
        }
        i = getDefaultVersion();
        if(i!=null){
            if(i!=status.isDefaultVersion()){
                return false;
            }
        }
        return true;
    }
}
