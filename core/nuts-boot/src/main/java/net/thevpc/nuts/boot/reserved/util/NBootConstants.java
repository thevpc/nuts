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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot.reserved.util;

/**
 * Boot Nuts constants. Represents various constants used in boot
 * <br>
 *
 * @author thevpc
 * @app.category Constants
 * @since 0.1.0
 */
public final class NBootConstants {

    /**
     * private constructor
     */
    private NBootConstants() {
    }

    /**
     * name constants
     *
     * @app.category Constants
     */
    public static final class Names {

        /**
         * default workspace name
         */
        public static final String DEFAULT_WORKSPACE_NAME = "default-workspace";
        /**
         * default repository name. By default a repository named "local" is
         * created as folder ~/.config/nuts/default-workspace/local (assuming
         * default workspace and default root naming on linux machine)
         */
        public static final String DEFAULT_REPOSITORY_NAME = "local";

        /**
         * private constructor
         */
        private Names() {
        }

    }

    /**
     * @app.category Constants
     */
    public static final class RepoTypes {

        /**
         * default repository type. Nuts workspace can managed different
         * repositories with different types. For instance, one can initialize a
         * default (nuts) repository along with a maven repository. Repository
         * Type expresses mainly manager subsystem type (nuts, maven, gradle,
         * zypper, apt-get, etc...)
         */
        public static final String NUTS = "nuts";

        /**
         * maven repository type.
         */
        public static final String MAVEN = "maven";

        /**
         * private constructor
         */
        private RepoTypes() {
        }
    }

    /**
     * @app.category Constants
     */
    public static final class RepoTags {

        /**
         * repo preview tag is used to help switching on or off dev/alpha/beta repositories
         */
        public static final String PREVIEW = "preview";
        public static final String MAIN = "main";
        public static final String LOCAL = "local";


        /**
         * private constructor
         */
        private RepoTags() {
        }
    }


    /**
     * Nuts Id query parameter names. Nuts id has the following form
     * group:name#version?query where query is in the form
     * key=value{@literal @}key=value...
     * <br>
     * This class defines all standard key names and their default values in the
     * query part.
     *
     * @app.category Constants
     */
    public static final class IdProperties {

        /**
         * id classifier (equivalent to maven classifier). The classifier
         * distinguishes artifacts that were built from the same POM but differ
         * in content. It's some optional and arbitrary string that - if
         * present - is appended to the artifact name just after the version
         * number.
         */
        public static final String CLASSIFIER = "classifier";

        /**
         * id face discriminator. The face defines the "content type" (aka the
         * face) to load for the same id, where face values are defined in
         * QueryFaces
         */
        public static final String FACE = "face";

        /**
         * id packaging (jar, war, ...)
         */
        public static final String PACKAGING = "packaging";

        /**
         * id platform. a platform is the runtime required to run the package on the operating system.
         * standard platforms are
         * <ul>
         * <li>java</li>
         * <li>dotnet</li>
         * </ul>
         */
        public static final String PLATFORM = "platform";

        /**
         * id profile. a profile is mostly defined by the build tool (maven or gradle).
         */
        public static final String PROFILE = "profile";

        /**
         * id supported operating system. standard values are :
         * <ul>
         * <li>linux</li>
         * <li>windows</li>
         * <li>mac</li>
         * <li>sunos</li>
         * <li>freebsd</li>
         * </ul>
         */
        public static final String OS = "os";

        /**
         * optional dependency. standard values are :
         * <ul>
         * <li>true</li>
         * </ul>
         * <br>
         * any other value, is interpreted as false.
         */
        public static final String OPTIONAL = "optional";

        /**
         * optional dependency. standard values are those defined in
         * NutsDependencyScope and NutsDependencyScopePattern.
         * <ul>
         * <li>api</li>
         * <li>implementation</li>
         * <li>provided</li>
         * <li>import</li>
         * <li>runtime</li>
         * <li>system</li>
         * <li>test-api</li>
         * <li>test-implementation</li>
         * <li>test-runtime</li>
         * <li>test-provided</li>
         * <li>test-system</li>
         * <li>test</li>
         * <li>compile</li>
         * <li>run</li>
         * </ul>
         * <br>
         * any other value, is interpreted as "api".
         */
        public static final String SCOPE = "scope";

        /**
         * id version can be defined as a property
         */
        public static final String VERSION = "version";

        /**
         * id repository can be defined as a property
         */
        public static final String REPO = "repo";

        /**
         * dependency exclusions
         */
        public static final String EXCLUSIONS = "exclusions";

        /**
         * id supported os distribution. mainly useful in linux oses. standard
         * values are :
         * <ul>
         * <li>opensuse-tumbleweed</li>
         * <li>opensuse-leap</li>
         * <li>redhat</li>
         * <li>centos</li>
         * <li>ubunto</li>
         * <li>debian</li>
         * <li>arch</li>
         * </ul>
         */
        public static final String OS_DIST = "osdist";

        /**
         * desktop environment
         */
        public static final String DESKTOP = "desktop";

        /**
         * id supported architecture. standard values are :
         * <ul>
         * <li>x86</li>
         * <li>ia64</li>
         * <li>amd64</li>
         * <li>ppc</li>
         * <li>sparc</li>
         * </ul>
         */
        public static final String ARCH = "arch";

        /**
         * dependency type
         */
        public static final String TYPE = "type";

        /**
         * condition properties
         *
         * @since 0.8.4
         */
        public static final String CONDITIONAL_PROPERTIES = "cond-properties";

        /**
         * private constructor
         */
        private IdProperties() {
        }
    }

    /**
     * valid values for Query parameter "face"
     *
     * @app.category Constants
     */
    public static final class QueryFaces {

        /**
         * package content face (jar, war file)
         */
        public static final String CONTENT = "content";

        /**
         * artifact descriptor (pom, nuts file)
         */
        public static final String DESCRIPTOR = "descriptor";

        /**
         * private constructor
         */
        private QueryFaces() {
        }
    }

    /**
     * file related constants
     *
     * @app.category Constants
     */
    public static final class Files {

        /**
         * workspace config file name
         */
        public static final String WORKSPACE_CONFIG_FILE_NAME = "nuts-workspace.json";

        /**
         * repository config file name
         */
        public static final String REPOSITORY_CONFIG_FILE_NAME = "nuts-repository.json";

        /**
         * nuts artifact descriptor file name
         */
        public static final String DESCRIPTOR_FILE_NAME = "nuts.json";

        /**
         * artifact descriptor file extension
         */
        public static final String DESCRIPTOR_FILE_EXTENSION = ".nuts";
        /**
         * workspace boot config file name for a particular nuts-api version
         */
        public static final String API_BOOT_CONFIG_FILE_NAME = "nuts-api-boot-config.json";
        /**
         * workspace boot config file name for a particular nuts-runtime version
         */
        public static final String RUNTIME_BOOT_CONFIG_FILE_NAME = "nuts-runtime-boot-config.json";
        /**
         * workspace boot config file name for a particular nuts-extension version
         */
        public static final String EXTENSION_BOOT_CONFIG_FILE_NAME = "nuts-extension-boot-config.json";

        /**
         * private constructor
         */
        private Files() {
        }
    }

    /**
     * identifier related constants
     *
     * @app.category Constants
     */
    public static final class Ids {

        public static final String NUTS_GROUP_ID = "net.thevpc.nuts";
        public static final String NUTS_API_ARTIFACT_ID = "nuts-api";
        public static final String NUTS_APP_ARTIFACT_ID = "nuts";
        public static final String NUTS_RUNTIME_ARTIFACT_ID = "nuts-runtime";
        /**
         * nuts api id
         */
        public static final String NUTS_API = NUTS_GROUP_ID+":"+NUTS_API_ARTIFACT_ID;
        public static final String NUTS_APP = NUTS_GROUP_ID+":"+NUTS_APP_ARTIFACT_ID;
        /**
         * nuts runtime id
         */
        public static final String NUTS_RUNTIME = NUTS_GROUP_ID+":"+NUTS_RUNTIME_ARTIFACT_ID;
        /**
         * nuts shell id
         */
        public static final String NUTS_SHELL = "net.thevpc.nuts.toolbox:nsh";

        /**
         * private constructor
         */
        private Ids() {
        }
    }

    /**
     * default folder names
     *
     * @app.category Constants
     */
    public static final class Folders {

        /**
         * folder that contains information about particular nuts artifacts.
         * each nuts artifact as its very own folder defined by its group, name
         * and version path parts
         */
        public static final String ID = "id";

        /**
         * private constructor
         */
        private Folders() {
        }
    }

    /**
     * version special names
     *
     * @app.category Constants
     */
    public static final class Versions {

        /**
         * latest version (inherited from maven)
         */
        public static final String LATEST = "LATEST";

        /**
         * release version (inherited from maven)
         */
        public static final String RELEASE = "RELEASE";

        /**
         * private constructor
         */
        private Versions() {
        }
    }


}
