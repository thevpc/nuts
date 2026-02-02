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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.security;

import net.thevpc.nuts.core.NConfigItem;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public final class NNamedCredentialConfig extends NConfigItem implements Cloneable{

    private static final long serialVersionUID = 2;
    private String name;
    private String resource;
    private String user;
    private String credential;

    public NNamedCredentialConfig() {
    }

    public NNamedCredentialConfig(NNamedCredentialConfig other) {
        this.user = other.getUser();
        this.credential = other.getCredential();
        this.name = other.getName();
        this.resource = other.getResource();
    }

    public NNamedCredentialConfig(String name, String resource, String user, String credential) {
        this.user = user;
        this.credential = credential;
        this.name = name;
        this.resource = resource;
    }

    public NNamedCredentialConfig copy() {
        return clone();
    }

    @Override
    protected NNamedCredentialConfig clone() {
        try {
            NNamedCredentialConfig o =(NNamedCredentialConfig) super.clone();
            return o;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getName() {
        return name;
    }

    public NNamedCredentialConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getResource() {
        return resource;
    }

    public NNamedCredentialConfig setResource(String resource) {
        this.resource = resource;
        return this;
    }


    @Override
    public String toString() {
        return "NutsUserConfig{" +
                "user='" + user + '\'' +
                ", credentials='" + credential + '\'' +
                '}';
    }
}
