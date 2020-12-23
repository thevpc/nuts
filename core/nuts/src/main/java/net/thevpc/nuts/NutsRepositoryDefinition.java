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
package net.thevpc.nuts;

/**
 *
 * @author thevpc
 * @since 0.5.4
 * @category Config
 */
public class NutsRepositoryDefinition {

    public static final int ORDER_USER_LOCAL = 1000;
    public static final int ORDER_SYSTEM_LOCAL = 2000;
    public static final int ORDER_USER_REMOTE = 10000;
    private String name;
    private String location;
    private String type;
    private boolean proxy;
    private boolean reference;
    private boolean failSafe;
    private boolean create;
    private boolean temporary;
    private int order;
    private NutsStoreLocationStrategy storeLocationStrategy;
    private int deployOrder = 100;
    private NutsSession session;

    public NutsRepositoryDefinition() {

    }

    public NutsRepositoryDefinition(NutsRepositoryDefinition o) {
        this.name = o.name;
        this.location = o.location;
        this.type = o.type;
        this.proxy = o.proxy;
        this.reference = o.reference;
        this.failSafe = o.failSafe;
        this.create = o.create;
        this.deployOrder = o.deployOrder;
        this.storeLocationStrategy = o.storeLocationStrategy;
        this.order = o.order;
        this.temporary = o.temporary;
        this.session = o.session;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsRepositoryDefinition setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public int getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public boolean isProxy() {
        return proxy;
    }

    public boolean isReference() {
        return reference;
    }

    public NutsRepositoryDefinition setReference(boolean reference) {
        this.reference = reference;
        return this;
    }

    public NutsRepositoryDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public NutsRepositoryDefinition setLocation(String location) {
        this.location = location;
        return this;
    }

    public NutsRepositoryDefinition setType(String type) {
        this.type = type;
        return this;
    }

    public NutsRepositoryDefinition setProxy(boolean proxy) {
        this.proxy = proxy;
        return this;
    }

    public NutsRepositoryDefinition setOrder(int order) {
        this.order = order;
        return this;
    }

    public boolean isFailSafe() {
        return failSafe;
    }

    public NutsRepositoryDefinition setFailSafe(boolean failSafe) {
        this.failSafe = failSafe;
        return this;
    }

    public boolean isCreate() {
        return create;
    }

    public NutsRepositoryDefinition setCreate(boolean create) {
        this.create = create;
        return this;
    }

    public int getDeployOrder() {
        return deployOrder;
    }

    public NutsRepositoryDefinition setDeployOrder(int deployPriority) {
        this.deployOrder = deployPriority;
        return this;
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NutsRepositoryDefinition setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public NutsRepositoryDefinition copy() {
        return new NutsRepositoryDefinition(this);
    }

    @Override
    public String toString() {
        return "NutsRepositoryDefinition{" + "name=" + name + ", location=" + location + ", type=" + type + ", proxy=" + proxy + ", reference=" + reference + ", failSafe=" + failSafe + ", create=" + create + ", order=" + order + ", storeLocationStrategy=" + storeLocationStrategy + ", deployOrder=" + deployOrder + '}';
    }

}
