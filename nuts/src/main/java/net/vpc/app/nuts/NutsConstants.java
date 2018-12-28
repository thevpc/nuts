/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vpc on 1/14/17.
 */
public final class NutsConstants {

    /**
     * Default workspace root folder. Workspaces are created as plain folders
     * under this root folder.
     */
    public static final String DEFAULT_NUTS_HOME = "~/.nuts";

    /**
     * default workspace name
     */
    public static final String DEFAULT_WORKSPACE_NAME = "default-workspace";

    /**
     * default repository name. By default a repository named "local" is created
     * as folder ~/.nuts/default-workspace/local (assuming default workspace and
     * default root naming)
     */
    public static final String DEFAULT_REPOSITORY_NAME = "local";

    /**
     * default repositories root name. By default repositories are stored under
     * ${workspace-location}/repositories or ${repository-location}/repositories
     */
    public static final String FOLDER_NAME_REPOSITORIES = "repositories";

    /**
     * default components root name. By default repositories are stored under
     * ${workspace-location}/components
     */
    public static final String FOLDER_NAME_COMPONENTS = "components";

    /**
     * default repository type. Nuts workspace can managed different
     * repositories with different types. For instance, one can initialize a
     * default (nuts) repository along with a maven repository. Repository Type
     * expresses mainly manager subsystem type (nuts, maven, gradle, zypper,
     * apt-get, etc...)
     */
    public static final String REPOSITORY_TYPE_NUTS = "nuts";
    public static final String REPOSITORY_TYPE_NUTS_FOLDER = "nuts-folder";
    public static final String REPOSITORY_TYPE_NUTS_SERVER = "nuts-server";
    public static final String REPOSITORY_TYPE_NUTS_MAVEN = "maven";

    /**
     * Installation Store
     */
    public static final String DEFAULT_STORE_PROGRAM = "programs";
    public static final String DEFAULT_STORE_TEMP = "temp";
    public static final String DEFAULT_STORE_VAR = "var";
    public static final String DEFAULT_STORE_LOG = "log";
    public static final String DEFAULT_STORE_CONFIG = "config";

    /**
     * workspace config file name
     */
    public static final String NUTS_WORKSPACE_CONFIG_FILE_NAME = "nuts-workspace.json";

    /**
     * repository config file name
     */
    public static final String NUTS_REPOSITORY_CONFIG_FILE_NAME = "nuts-repository.json";

    /**
     * repository config file name
     */
    public static final String NUTS_SHELL = "net.vpc.app.nuts.toolbox:nsh";

    /**
     * component (nuts) descriptor file name
     */
    public static final String NUTS_DESC_FILE_NAME = "nuts.json";

    public static final String RIGHT_FETCH_DESC = "fetch-desc";
    public static final String RIGHT_FETCH_CONTENT = "fetch-content";
    public static final String RIGHT_SAVE_REPOSITORY = "save";
    public static final String RIGHT_SAVE_WORKSPACE = "save";
    public static final String RIGHT_AUTO_INSTALL = "auto-install";
    public static final String RIGHT_INSTALL = "install";
    public static final String RIGHT_UNINSTALL = "uninstall";
    public static final String RIGHT_EXEC = "exec";
    public static final String RIGHT_DEPLOY = "deploy";
    public static final String RIGHT_UNDEPLOY = "undeploy";
    public static final String RIGHT_PUSH = "push";
    public static final String RIGHT_ADD_REPOSITORY = "add-repo";
    public static final String RIGHT_REMOVE_REPOSITORY = "remove-repo";
    public static final String RIGHT_SET_PASSWORD = "set-password";
    public static final String RIGHT_ADMIN = "admin";
    public static final String[] RIGHTS = {RIGHT_FETCH_DESC, RIGHT_FETCH_CONTENT, RIGHT_SAVE_REPOSITORY,
        RIGHT_SAVE_WORKSPACE, RIGHT_INSTALL, RIGHT_AUTO_INSTALL,RIGHT_UNINSTALL, RIGHT_EXEC, RIGHT_DEPLOY, RIGHT_UNDEPLOY,
        RIGHT_PUSH, RIGHT_ADD_REPOSITORY, RIGHT_REMOVE_REPOSITORY, RIGHT_SET_PASSWORD, RIGHT_ADMIN};

    public static final String ENV_KEY_EXCLUDE_CORE_EXTENSION = "exclude-core-extension";
    public static final String ENV_KEY_AUTOSAVE = "autosave";
    public static final String ENV_KEY_PASSPHRASE = "passphrase";
    public static final String ENV_KEY_DEPLOY_PRIORITY = "deploy-priority";
    public static final String ENV_STORE_PROGRAMS = "workspace-programs";
    public static final String ENV_STORE_VAR = "workspace-var";
    public static final String ENV_STORE_LOGS = "workspace-logs";
    public static final String ENV_STORE_TEMP = "workspace-temp";
    public static final String ENV_STORE_CONFIG = "workspace-config";

    public static final String DEFAULT_HTTP_SERVER = "nuts-http-server";
    public static final int DEFAULT_HTTP_SERVER_PORT = 8899;
    public static final String DEFAULT_ADMIN_SERVER = "nuts-admin-server";
    public static final int DEFAULT_ADMIN_SERVER_PORT = 8898;

    public static final String USER_ADMIN = "admin";
    public static final String USER_ANONYMOUS = "anonymous";

    public static final String NUTS_ID_BOOT_API = "net.vpc.app.nuts:nuts";
    public static final String NUTS_ID_BOOT_API_PATH = "/"+ NUTS_ID_BOOT_API.replaceAll("[.:]","/");
    public static final String NUTS_ID_BOOT_RUNTIME = "net.vpc.app.nuts:nuts-core";
    public static final String QUERY_FACE = "face";
    public static final String QUERY_ARCH = "arch";
    public static final String QUERY_OS = "os";
    public static final String QUERY_OSDIST = "osdist";
    public static final String QUERY_PLATFORM = "platform";
    public static final String QUERY_FACE_DEFAULT_VALUE = "default";
    public static final String QUERY_FILE = "file";
    public static final String VERSION_CHECKED_OUT_EXTENSION = "-CHECKED-OUT";
    public static final String URL_BOOTSTRAP_REMOTE_NUTS_GIT = "https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master";
    public static final String URL_BOOTSTRAP_REMOTE_MAVEN_GIT = "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master";
//    public static final String URL_BOOTSTRAP_REMOTE_MAVEN_GIT = "https://github.com/thevpc/vpc-public-maven/raw/master/";
    public static final String URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL = "http://repo.maven.apache.org/maven2/";
    public static final String URL_BOOTSTRAP_LOCAL_MAVEN_CENTRAL = "~/.m2/repository";
    public static final String URL_BOOTSTRAP_LOCAL = "~/.nuts/remote-bootstrap";
    public static final String URL_COMPONENTS_REMOTE = URL_BOOTSTRAP_REMOTE_MAVEN_CENTRAL+";"+ URL_BOOTSTRAP_REMOTE_NUTS_GIT;
//    public static final String URL_COMPONENTS_LOCAL = URL_BOOTSTRAP_LOCAL_MAVEN_CENTRAL//+";"+DEFAULT_NUTS_HOME+"/bootstrap";

    private static final Map<String, String> _QUERY_EMPTY_ENV = new HashMap<>();
    public static final Map<String, String> QUERY_EMPTY_ENV = Collections.unmodifiableMap(_QUERY_EMPTY_ENV);
    public static final String NUTS_COMMAND_FILE_EXTENSION = ".njc";

    static {
        _QUERY_EMPTY_ENV.put(NutsConstants.QUERY_ARCH, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.QUERY_OS, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.QUERY_OSDIST, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.QUERY_PLATFORM, null);
    }

    private NutsConstants() {
    }
}
