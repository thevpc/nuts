/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vpc on 1/14/17.
 */
public class NutsConstants {

    /**
     * Default workspace root folder. Workspaces are created as plain forlders
     * under this root folder.
     */
    public static final String DEFAULT_WORKSPACE_ROOT = "~/.nuts";

    /**
     * default workspace name
     */
    public static final String DEFAULT_WORKSPACE_NAME = "default-workspace";

    /**
     * default workspace name
     */
    public static final String BOOTSTRAP_WORKSPACE_NAME = "bootstrap";
    public static final String BOOTSTRAP_REPOSITORY_NAME = "bootstrap";

    /**
     * default repository name. By default a repository named
     * 'default-workspace' is created as folder
     * ~/.nuts/default-workspace/default-repository (assuming default workspace
     * and default root naming)
     */
    public static final String DEFAULT_REPOSITORY_NAME = "local";

    /**
     * default repositories root name. By default repositories are stored under
     * ${workspace-location}/repositories or ${repository-location}/repositories
     */
    public static final String DEFAULT_REPOSITORIES_ROOT = "repositories";

    /**
     * default components root name. By default repositories are stored under
     * ${workspace-location}/components
     */
    public static final String DEFAULT_COMPONENTS_ROOT = "components";

    /**
     * default repository type. Nuts workspace can managed different
     * repositories with different types. For instance, one can initialize a
     * default (nuts) repository along with a maven repository. Repository Type
     * expresses mainly manager subsystem type (nuts, maven, gradle, zypper,
     * apt-get, etc...)
     */
    public static final String DEFAULT_REPOSITORY_TYPE = "nuts";

    /**
     * Installation Store
     */
    public static final String DEFAULT_STORE_ROOT = "store";

    /**
     * workspace config file name
     */
    public static final String NUTS_WORKSPACE_FILE = "nuts-workspace.json";

    /**
     * repository config file name
     */
    public static final String NUTS_REPOSITORY_FILE = "nuts-repository.json";

    /**
     * component (nuts) descriptor file name
     */
    public static final String NUTS_DESC_FILE = "nuts.json";

    public static final String RIGHT_FETCH_DESC = "fetch-desc";
    public static final String RIGHT_FETCH_CONTENT = "fetch-content";
    public static final String RIGHT_SAVE_REPOSITORY = "save";
    public static final String RIGHT_SAVE_WORKSPACE = "save";
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
        RIGHT_SAVE_WORKSPACE, RIGHT_INSTALL, RIGHT_UNINSTALL, RIGHT_EXEC, RIGHT_DEPLOY, RIGHT_UNDEPLOY,
        RIGHT_PUSH, RIGHT_ADD_REPOSITORY, RIGHT_REMOVE_REPOSITORY, RIGHT_SET_PASSWORD, RIGHT_ADMIN};

    public static final String ENV_KEY_EXCLUDE_CORE_EXTENSION = "exclude-core-extension";
    public static final String ENV_KEY_AUTOSAVE = "autosave";
    public static final String ENV_KEY_PASSPHRASE = "passphrase";
    public static final String ENV_KEY_DEPLOY_PRIORITY = "deploy-priority";
    public static final String ENV_STORE = "workspace-store";

    public static final String DEFAULT_HTTP_SERVER = "nuts-http-server";
    public static final int DEFAULT_HTTP_SERVER_PORT = 8899;
    public static final String DEFAULT_ADMIN_SERVER = "nuts-admin-server";
    public static final int DEFAULT_ADMIN_SERVER_PORT = 8898;

    public static final String USER_ADMIN = "admin";
    public static final String USER_ANONYMOUS = "anonymous";

    public static final String NUTS_COMPONENT_ID = "net.vpc.app.nuts:nuts";
    public static final String NUTS_COMPONENT_CORE_ID = "net.vpc.app.nuts:nuts-core";
    public static final String QUERY_FACE = "face";
    public static final String QUERY_ARCH = "arch";
    public static final String QUERY_OS = "os";
    public static final String QUERY_OSDIST = "osdist";
    public static final String QUERY_PLATFORM = "platform";
    public static final String QUERY_FACE_DEFAULT_VALUE = "default";
    public static final String QUERY_FILE = "file";
    public static final String VERSION_CHECKED_OUT_EXTENSION = "-CHECKED-OUT";

    private static final Map<String, String> _QUERY_EMPTY_ENV = new HashMap<>();
    public static final Map<String, String> QUERY_EMPTY_ENV = Collections.unmodifiableMap(_QUERY_EMPTY_ENV);

    static {
        _QUERY_EMPTY_ENV.put(NutsConstants.QUERY_ARCH, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.QUERY_OS, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.QUERY_OSDIST, null);
        _QUERY_EMPTY_ENV.put(NutsConstants.QUERY_PLATFORM, null);
    }
}
