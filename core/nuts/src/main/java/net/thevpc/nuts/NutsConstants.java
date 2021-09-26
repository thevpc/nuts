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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Common Nuts constants. Represents various constants used in runtime
 * implementation.
 * <br>
 * Created by vpc on 1/14/17.
 *
 * @since 0.1.0
 * @app.category Constants
 */
public final class NutsConstants {

    /**
     * private constructor
     */
    private NutsConstants() {
    }

    /**
     * name constants
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
     * Nuts Id query parameter names. Nuts id has the following form
     * group:name#version?query where query is in the form
     * key=value{@literal @}key=value...
     * <br>
     * This class defines all standard key names and their default values in the
     * query part.
     * @app.category Constants
     */
    public static final class IdProperties {

        /**
         * id classifier (equivalent to maven classifier). The classifier
         * distinguishes artifacts that were built from the same POM but differ
         * in content. It is some optional and arbitrary string that - if
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

//        /**
//         * id alternative is a second degree classifier that helps providing
//         * multiple alternatives (based on arch, platform,....) for the same
//         * package. The alternative defines distinct descriptors with the same
//         * id.
//         */
//        public static final String ALTERNATIVE = "alt";

//        /**
//         * id alternative default value. the default value is equivalent to
//         * missing value.
//         */
//        public static final String ALTERNATIVE_DEFAULT_VALUE = "default";

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

        public static final String DESKTOP_ENVIRONMENT = "de";

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
         * private constructor
         */
        private IdProperties() {
        }
    }

    /**
     * valid values for Query parameter "face"
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
         * artifact content hash face (jar, war hash file)
         */
        public static final String CONTENT_HASH = "content-hash";

        /**
         * artifact descriptor hash (pom, nuts hash file)
         */
        public static final String DESCRIPTOR_HASH = "descriptor-hash";

        /**
         * private constructor
         */
        private QueryFaces() {
        }
    }

    /**
     * @app.category Constants
     */
    public static final class BootstrapURLs {

        /**
         * apache maven2 repository. contains mainly standard (community) java
         * artifacts.
         */
        public static final String REMOTE_MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2";

        /**
         * vpc-public-maven git repository contains mainly nuts author non java
         * artifacts.
         */

        /**
         * maven local repository at ~/.m2/repository
         */
        public static final String LOCAL_MAVEN_CENTRAL = "~/.m2/repository";


        /**
         * private constructor
         */
        private BootstrapURLs() {
        }
    }

    /**
     * nuts standard user names
     * @app.category Constants
     */
    public static final class Users {

        /**
         * administrator/root user with ALL privileges
         */
        public static final String ADMIN = "admin";

        /**
         * non authenticated (anonymous) user with NO privileges. However in
         * "unsecure" mode, all users including anonymous will aquire all
         * privileges though.
         */
        public static final String ANONYMOUS = "anonymous";

        /**
         * private constructor
         */
        private Users() {
        }
    }

    /**
     * standard right keys for distinct operations in nuts.
     * @app.category Constants
     */
    public static final class Permissions {

        /**
         * all fetch descriptors (all information but content)
         */
        public static final String FETCH_DESC = "fetch-desc";

        /**
         * all fetch descriptors (all information but content)
         */
        public static final String FETCH_CONTENT = "fetch-content";

        /**
         * save workspace and repository
         */
        public static final String SAVE = "save";

        /**
         * auto install artifact when exec is requested
         */
        public static final String AUTO_INSTALL = "auto-install";

        /**
         * install artifact
         */
        public static final String INSTALL = "install";

        /**
         * update artifact
         */
        public static final String UPDATE = "update";

        /**
         * uninstall artifact
         */
        public static final String UNINSTALL = "uninstall";

        /**
         * exec artifact
         */
        public static final String EXEC = "exec";

        /**
         * deploy artifact
         */
        public static final String DEPLOY = "deploy";

        /**
         * undeploy artifact
         */
        public static final String UNDEPLOY = "undeploy";

        /**
         * push artifact
         */
        public static final String PUSH = "push";

        /**
         * add repository
         */
        public static final String ADD_REPOSITORY = "add-repo";

        /**
         * remove repository
         */
        public static final String REMOVE_REPOSITORY = "remove-repo";

        /**
         * update password
         */
        public static final String SET_PASSWORD = "set-password";

        /**
         * admin right
         */
        public static final String ADMIN = "admin";

        /**
         * all permissions set
         */
        public static final Set<String> ALL = Collections.unmodifiableSet(
                new HashSet<>(Arrays.asList(FETCH_DESC, FETCH_CONTENT, SAVE, INSTALL, UPDATE, AUTO_INSTALL, UNINSTALL, EXEC, DEPLOY, UNDEPLOY,
                        PUSH, ADD_REPOSITORY, REMOVE_REPOSITORY, SET_PASSWORD, ADMIN)));

        /**
         * private constructor
         */
        private Permissions() {
        }
    }

    /**
     * file related constants
     * @app.category Constants
     */
    public static final class Files {

        /**
         * workspace config file name
         */
        public static final String WORKSPACE_CONFIG_FILE_NAME = "nuts-workspace.json";

        /**
         * workspace boot config file name for a particular nuts-api version
         */
        public static final String WORKSPACE_API_CONFIG_FILE_NAME = "nuts-api-config.json";

        /**
         * workspace boot config file name for a particular nuts-runtime version
         */
        public static final String WORKSPACE_RUNTIME_CONFIG_FILE_NAME = "nuts-runtime-config.json";

        /**
         * workspace boot config file name for a particular nuts-extension version
         */
        public static final String WORKSPACE_EXTENSION_CACHE_FILE_NAME = "nuts-extension-cache.json";
        /**
         * workspace boot config file name for a particular nuts-extension version
         */
        public static final String WORKSPACE_RUNTIME_CACHE_FILE_NAME = "nuts-runtime-cache.json";

        /**
         * repository config file name
         */
        public static final String REPOSITORY_CONFIG_FILE_NAME = "nuts-repository.json";

        /**
         * nuts command alias file extension
         */
        public static final String NUTS_COMMAND_FILE_EXTENSION = ".nuts-cmd.json";

        /**
         * nuts artifact descriptor file name
         */
        public static final String DESCRIPTOR_FILE_NAME = "nuts.json";

        /**
         * artifact descriptor file extension
         */
        public static final String DESCRIPTOR_FILE_EXTENSION = ".nuts";

        /**
         * private constructor
         */
        private Files() {
        }
    }

    /**
     * identifier related constants
     * @app.category Constants
     */
    public static final class Ids {

        /**
         * nuts api id
         */
        public static final String NUTS_API = "net.thevpc.nuts:nuts";
        /**
         * nuts runtime id
         */
        public static final String NUTS_RUNTIME = "net.thevpc.nuts:nuts-runtime";
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
     * @app.category Constants
     */
    public static final class Folders {

        /**
         * bootstrap cache folder name. Typically this is stored at
         * ${workspace-location-cache}/boot
         */
        public static final String BOOT = "boot";
        /**
         * default repositories root name. By default repositories are stored
         * under ${workspace-location-config}/repos
         * (${workspace-location}/config/repos for standalone workspaces).
         * Repository mirrors are stored under
         * ${repository-config-location}/repos
         */
        public static final String REPOSITORIES = "repos";

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

    /**
     * version special names
     * @app.category Constants
     */
    public static final class Ntf {

        /**
         * latest version (inherited from maven)
         */
//        public static final char SILENT = '¤';//ø
        public static final char SILENT = '\u001E';//record separator

        /**
         * private constructor
         */
        private Ntf() {
        }
    }
}
