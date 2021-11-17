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
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsParseEnumException;
import net.thevpc.nuts.NutsSession;

/**
 *
 * @author thevpc
 */
public enum NutsDisplayProperty  implements NutsEnum {
    STATUS,
    INSTALL_DATE,
    INSTALL_USER,
    INSTALL_FOLDER,
    REPOSITORY,
    REPOSITORY_ID,
    ID,
    NAME,
    ARCH,
    PACKAGING,
    PLATFORM,
    DESKTOP_ENVIRONMENT,
    OS,
    OSDIST,
    EXEC_ENTRY,
    FILE_NAME,
    FILE,
    VAR_LOCATION,
    TEMP_FOLDER,
    CONFIG_FOLDER,
    LIB_FOLDER,
    LOG_FOLDER,
    CACHE_FOLDER,
    APPS_FOLDER,
    LONG_STATUS;
    private String id;

    NutsDisplayProperty() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    @Override
    public String id() {
        return id;
    }

    public static NutsDisplayProperty parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsDisplayProperty parseLenient(String value, NutsDisplayProperty emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsDisplayProperty parseLenient(String value, NutsDisplayProperty emptyValue, NutsDisplayProperty errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsDisplayProperty.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            //
        }
        switch (value){
            case "de":return DESKTOP_ENVIRONMENT;
            case "osdist":return OSDIST;
            case "repo":return REPOSITORY;
            case "repo_id":return REPOSITORY_ID;
        }
        return errorValue;
    }

    public static NutsDisplayProperty parse(String value, NutsSession session) {
        return parse(value, null,session);
    }

    public static NutsDisplayProperty parse(String value, NutsDisplayProperty emptyValue, NutsSession session) {
        NutsDisplayProperty v = parseLenient(value, emptyValue, null);
        if(v==null){
            if(!NutsBlankable.isBlank(value)){
                throw new NutsParseEnumException(session,value,NutsDisplayProperty.class);
            }
        }
        return v;
    }
}
