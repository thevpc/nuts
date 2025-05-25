/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * uniform platform
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.1
 */
public enum NPlatformFamily implements NEnum {
    /**
     * Represents the operating system platform family.
     * This is used to classify and identify operating system-based platforms.
     */
    OS,
    /**
     * Represents the Java platform family.
     * This is used to classify and identify Java-based platforms,
     * including JDK, JRE, and related technologies.
     */
    JAVA,
    /**
     * Represents the .NET platform family.
     * This is used to classify and identify .NET-based platforms,
     * including frameworks such as .NET Core, .NET Framework, and associated technologies.
     */
    DOTNET,
    /**
     * Represents the Python platform family.
     * This is used to classify and identify Python-based platforms,
     * including implementations such as CPython, PyPy, and related technologies.
     */
    PYTHON,
    /**
     * Represents the JavaScript platform family.
     * This is used to classify and identify JavaScript-based platforms,
     * including runtime environments such as Node.js, browsers, and related technologies.
     */
    JAVASCRIPT,
    /**
     * Represents an unknown platform family.
     * This is used as a fallback for platforms that do not match any
     * of the predefined categories or are unrecognized.
     */
    UNKNOWN;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NPlatformFamily() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NPlatformFamily> parse(String value) {
        return NEnumUtils.parseEnum(value, NPlatformFamily.class, s->{
            switch (s.getNormalizedValue()) {
                case "OS":
                    return NOptional.of(OS);

                case "JAVA":
                case "JAVAW":
                case "JRE":
                case "JDK":
                case "OPENJDK":
                    return NOptional.of(JAVA);

                case "NET":
                case "DOTNET":
                    return NOptional.of(DOTNET);

                case "PYTHON":
                    return NOptional.of(PYTHON);

                case "JS":
                case "JAVASCRIPT":
                    return NOptional.of(JAVASCRIPT);

                case "UNKNOWN":
                    return NOptional.of(UNKNOWN);
            }
            return null;
        });
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
