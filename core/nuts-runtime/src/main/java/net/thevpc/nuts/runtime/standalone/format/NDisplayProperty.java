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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 *
 * @author thevpc
 */
public enum NDisplayProperty implements NEnum {
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
    CONF_FOLDER,
    LIB_FOLDER,
    LOG_FOLDER,
    CACHE_FOLDER,
    BIN_FOLDER,
    LONG_STATUS;
    private String id;

    NDisplayProperty() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NOptional<NDisplayProperty> parse(String value) {
        return NEnumUtils.parseEnum(value, NDisplayProperty.class, s->{
            switch (s.getNormalizedValue()){
                case "DE":
                case "DESKTOP":
                    return NOptional.of(DESKTOP_ENVIRONMENT);
                case "OSDIST":return NOptional.of(OSDIST);
                case "REPO":return NOptional.of(REPOSITORY);
                case "REPO_ID":
                case "REPOID":
                    return NOptional.of(REPOSITORY_ID);
            }
            return null;
        });
    }
}
