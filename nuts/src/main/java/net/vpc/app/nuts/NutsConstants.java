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

/**
 * Created by vpc on 1/14/17.
 *
 * @since 0.1.0
 */
public final class NutsConstants {

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

        private Names() {
        }

    }

    public static final class RepoTypes {

        private RepoTypes() {
        }

        /**
         * default repository type. Nuts workspace can managed different
         * repositories with different types. For instance, one can initialize a
         * default (nuts) repository along with a maven repository. Repository
         * Type expresses mainly manager subsystem type (nuts, maven, gradle,
         * zypper, apt-get, etc...)
         */
        public static final String NUTS = "nuts";
        public static final String NUTS_SERVER = "nuts-server";
        public static final String MAVEN = "maven";
    }

    public static final class QueryKeys {

        private QueryKeys() {
        }
        public static final String CLASSIFIER = "classifier";
        public static final String ALTERNATIVE_DEFAULT_VALUE = "default";
        public static final String PACKAGING = "packaging";
        public static final String PLATFORM = "platform";
        public static final String OS = "os";
        public static final String OPTIONAL = "optional";
        public static final String SCOPE = "scope";
        public static final String ALTERNATIVE = "alt";
        public static final String OSDIST = "osdist";
        public static final String FACE = "face";
        public static final String FACE_DEFAULT_VALUE = "default";
        public static final String ARCH = "arch";

    }

    public static final class QueryFaces {

        private QueryFaces() {
        }

        public static final String COMPONENT_HASH = "component-hash";
        public static final String COMPONENT = "component";
        public static final String DESCRIPTOR = "descriptor";
        public static final String DESC_HASH = "descriptor-hash";
        public static final String CATALOG = "catalog";

    }

    public static final class BootstrapURLs {

        private BootstrapURLs() {
        }

        public static final String REMOTE_NUTS_GIT = "https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master";
        public static final String LOCAL_NUTS_FOLDER = "${home.config}/.vpc-public-nuts";
        public static final String REMOTE_MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/";
        public static final String REMOTE_MAVEN_GIT = "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master";
        public static final String LOCAL_MAVEN_CENTRAL = "~/.m2/repository";

    }

    public static final class Users {

        public static final String ADMIN = "admin";
        public static final String ANONYMOUS = "anonymous";
    }

    public static final class Rights {

        private Rights() {
        }

        public static final String FETCH_DESC = "fetch-desc";
        public static final String FETCH_CONTENT = "fetch-content";
        public static final String SAVE_REPOSITORY = "save";
        public static final String SAVE_WORKSPACE = "save";
        public static final String AUTO_INSTALL = "auto-install";
        public static final String INSTALL = "install";
        public static final String UPDATE = "update";
        public static final String UNINSTALL = "uninstall";
        public static final String EXEC = "exec";
        public static final String DEPLOY = "deploy";
        public static final String UNDEPLOY = "undeploy";
        public static final String PUSH = "push";
        public static final String ADD_REPOSITORY = "add-repo";
        public static final String REMOVE_REPOSITORY = "remove-repo";
        public static final String SET_PASSWORD = "set-password";
        public static final String ADMIN = "admin";
        public static final String[] RIGHTS = {FETCH_DESC, FETCH_CONTENT, SAVE_REPOSITORY,
            SAVE_WORKSPACE, INSTALL, UPDATE, AUTO_INSTALL, UNINSTALL, EXEC, DEPLOY, UNDEPLOY,
            PUSH, ADD_REPOSITORY, REMOVE_REPOSITORY, SET_PASSWORD, ADMIN};

    }

    public static final class Files {

        /**
         * workspace config file name
         */
        public static final String WORKSPACE_CONFIG_FILE_NAME = "nuts-workspace.json";
        /**
         * repository config file name
         */
        public static final String REPOSITORY_CONFIG_FILE_NAME = "nuts-repository.json";
        public static final String NUTS_COMMAND_FILE_EXTENSION = ".njc";
        /**
         * component (nuts) descriptor file name
         */
        public static final String DESCRIPTOR_FILE_NAME = "nuts.json";
        public static final String DESCRIPTOR_FILE_EXTENSION = ".nuts";

    }

    public static final class Ids {

        private Ids() {
        }

        /**
         * nuts api id
         */
        public static final String NUTS_API = "net.vpc.app.nuts:nuts";
        /**
         * nuts runtime id
         */
        public static final String NUTS_RUNTIME = "net.vpc.app.nuts:nuts-core";
        /**
         * nuts shell id
         */
        public static final String NUTS_SHELL = "net.vpc.app.nuts.toolbox:nsh";

    }

    public static final class Folders {

        private Folders() {
        }
        
        /**
         * bootstrap cache folder name. Typically this is stored at 
         * ${workspace-location-cache}/boot
         */
        public static final String BOOT = "boot";
        /**
         * default repositories root name. By default repositories are stored
         * under ${workspace-location-config}/repos 
         * (${workspace-location}/config/repos for standalone workspaces).
         * Repository mirros are stored under  ${repository-config-location}/repos
         */
        public static final String REPOSITORIES = "repos";

        /**
         * folder that contains information about particular nuts components.
         * each nuts component as its very own folder defined by its group, name
         * and version path parts
         */
        public static final String ID = "id";

    }

    public static final class Versions {

        private Versions() {
        }
        public static final String LATEST = "LATEST";
        public static final String RELEASE = "RELEASE";
    }

    private NutsConstants() {
    }
}
