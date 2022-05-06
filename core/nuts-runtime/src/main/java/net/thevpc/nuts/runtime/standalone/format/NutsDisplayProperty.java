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

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsNameFormat;
import net.thevpc.nuts.util.NutsStringUtils;

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
    PROFILE,
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
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NutsOptional<NutsDisplayProperty> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsDisplayProperty.class, s->{
            switch (s.getNormalizedValue()){
                case "DE":
                case "DESKTOP":
                    return NutsOptional.of(DESKTOP_ENVIRONMENT);
                case "OSDIST":return NutsOptional.of(OSDIST);
                case "REPO":return NutsOptional.of(REPOSITORY);
                case "REPO_ID":
                case "REPOID":
                    return NutsOptional.of(REPOSITORY_ID);
            }
            return null;
        });
    }
}
