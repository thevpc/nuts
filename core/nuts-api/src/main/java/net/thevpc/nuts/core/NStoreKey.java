package net.thevpc.nuts.core;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.platform.NStoreScope;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Objects;

/**
 * NStoreKey class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NStoreKey {
    private final String name;
    private final NId id;
    private final String repoUuid;
    private final NStoreScope storeScope;
    private final NStoreType storeType;

    /**
     * Creates a new instance of of workspace.
     *
     * @return of workspace result
     */
    public static NStoreKey ofWorkspace() {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.WORKSPACE n store scope.workspace
         * @return of result
         */
        return of(NStoreScope.WORKSPACE);
    }

    /**
     * Creates a new instance of of system.
     *
     * @return of system result
     */
    public static NStoreKey ofSystem() {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.SYSTEM n store scope.system
         * @return of result
         */
        return of(NStoreScope.SYSTEM);
    }

    /**
     * Creates a new instance of of user.
     *
     * @return of user result
     */
    public static NStoreKey ofUser() {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.USER n store scope.user
         * @return of result
         */
        return of(NStoreScope.USER);
    }

    /**
     * Creates a new instance of of shared workspace.
     *
     * @param id id
     * @return of shared workspace result
     */
    public static NStoreKey ofSharedWorkspace(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.WORKSPACE).sharedId(id n store scope.workspace).shared id(id
         * @return of result
         */
        return of(NStoreScope.WORKSPACE).sharedId(id);
    }

    /**
     * Creates a new instance of of shared user.
     *
     * @param id id
     * @return of shared user result
     */
    public static NStoreKey ofSharedUser(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.USER).sharedId(id n store scope.user).shared id(id
         * @return of result
         */
        return of(NStoreScope.USER).sharedId(id);
    }

    /**
     * Creates a new instance of of shared system.
     *
     * @param id id
     * @return of shared system result
     */
    public static NStoreKey ofSharedSystem(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.SYSTEM).sharedId(id n store scope.system).shared id(id
         * @return of result
         */
        return of(NStoreScope.SYSTEM).sharedId(id);
    }

    /**
     * Creates a new instance of of workspace.
     *
     * @param id id
     * @return of workspace result
     */
    public static NStoreKey ofWorkspace(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.WORKSPACE).id(id n store scope.workspace).id(id
         * @return of result
         */
        return of(NStoreScope.WORKSPACE).id(id);
    }

    /**
     * Creates a new instance of of system.
     *
     * @param id id
     * @return of system result
     */
    public static NStoreKey ofSystem(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.SYSTEM).id(id n store scope.system).id(id
         * @return of result
         */
        return of(NStoreScope.SYSTEM).id(id);
    }

    /**
     * Creates a new instance of of user.
     *
     * @param id id
     * @return of user result
     */
    public static NStoreKey ofUser(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.USER).id(id n store scope.user).id(id
         * @return of result
         */
        return of(NStoreScope.USER).id(id);
    }

    /**
     * Creates a new instance of of user.
     *
     * @param storeType store type
     * @return of user result
     */
    public static NStoreKey ofUser(NStoreType storeType) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.USER).type(storeType n store scope.user).type(store type
         * @return of result
         */
        return of(NStoreScope.USER).type(storeType);
    }

    /**
     * Creates a new instance of of system.
     *
     * @param storeType store type
     * @return of system result
     */
    public static NStoreKey ofSystem(NStoreType storeType) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.SYSTEM).type(storeType n store scope.system).type(store type
         * @return of result
         */
        return of(NStoreScope.SYSTEM).type(storeType);
    }

    /**
     * Creates a new instance of of base.
     *
     * @param storeType store type
     * @return of base result
     */
    public static NStoreKey ofBase(NStoreType storeType) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.BASE).type(storeType n store scope.base).type(store type
         * @return of result
         */
        return of(NStoreScope.BASE).type(storeType);
    }

    /**
     * Creates a new instance of of workspace.
     *
     * @param storeType store type
     * @return of workspace result
     */
    public static NStoreKey ofWorkspace(NStoreType storeType) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreScope.WORKSPACE).type(storeType n store scope.workspace).type(store type
         * @return of result
         */
        return of(NStoreScope.WORKSPACE).type(storeType);
    }

    /**
     * Creates a new instance of of.
     *
     * @param storeScope store scope
     * @return of result
     */
    public static NStoreKey of(NStoreScope storeScope) {
        return new NStoreKey(storeScope == null ? NStoreScope.WORKSPACE : storeScope, NStoreType.CONF, null, null, null);
    }

    /**
     * Creates a new instance of of conf.
     *
     * @return of conf result
     */
    public static NStoreKey ofConf() {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.CONF n store type.conf
         * @return of result
         */
        return of(NStoreType.CONF);
    }

    /**
     * Creates a new instance of of bin.
     *
     * @return of bin result
     */
    public static NStoreKey ofBin() {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.BIN n store type.bin
         * @return of result
         */
        return of(NStoreType.BIN);
    }

    /**
     * Creates a new instance of of cache.
     *
     * @return of cache result
     */
    public static NStoreKey ofCache() {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.CACHE n store type.cache
         * @return of result
         */
        return of(NStoreType.CACHE);
    }

    /**
     * Creates a new instance of of var.
     *
     * @return of var result
     */
    public static NStoreKey ofVar() {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.VAR n store type.var
         * @return of result
         */
        return of(NStoreType.VAR);
    }

    /**
     * Creates a new instance of of log.
     *
     * @return of log result
     */
    public static NStoreKey ofLog() {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.LOG n store type.log
         * @return of result
         */
        return of(NStoreType.LOG);
    }

    /**
     * Creates a new instance of of run.
     *
     * @return of run result
     */
    public static NStoreKey ofRun() {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.RUN n store type.run
         * @return of result
         */
        return of(NStoreType.RUN);
    }

    /**
     * Creates a new instance of of temp.
     *
     * @return of temp result
     */
    public static NStoreKey ofTemp() {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.TEMP n store type.temp
         * @return of result
         */
        return of(NStoreType.TEMP);
    }

    /**
     * Creates a new instance of of lib.
     *
     * @return of lib result
     */
    public static NStoreKey ofLib() {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.LIB n store type.lib
         * @return of result
         */
        return of(NStoreType.LIB);
    }

    /**
     * Creates a new instance of of conf.
     *
     * @param id id
     * @return of conf result
     */
    public static NStoreKey ofConf(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.CONF).id(id n store type.conf).id(id
         * @return of result
         */
        return of(NStoreType.CONF).id(id);
    }

    /**
     * Creates a new instance of of bin.
     *
     * @param id id
     * @return of bin result
     */
    public static NStoreKey ofBin(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.BIN).id(id n store type.bin).id(id
         * @return of result
         */
        return of(NStoreType.BIN).id(id);
    }

    /**
     * Creates a new instance of of cache.
     *
     * @param id id
     * @return of cache result
     */
    public static NStoreKey ofCache(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.CACHE).id(id n store type.cache).id(id
         * @return of result
         */
        return of(NStoreType.CACHE).id(id);
    }

    /**
     * Creates a new instance of of var.
     *
     * @param id id
     * @return of var result
     */
    public static NStoreKey ofVar(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.VAR).id(id n store type.var).id(id
         * @return of result
         */
        return of(NStoreType.VAR).id(id);
    }

    /**
     * Creates a new instance of of log.
     *
     * @param id id
     * @return of log result
     */
    public static NStoreKey ofLog(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.LOG).id(id n store type.log).id(id
         * @return of result
         */
        return of(NStoreType.LOG).id(id);
    }

    /**
     * Creates a new instance of of run.
     *
     * @param id id
     * @return of run result
     */
    public static NStoreKey ofRun(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.RUN).id(id n store type.run).id(id
         * @return of result
         */
        return of(NStoreType.RUN).id(id);
    }

    /**
     * Creates a new instance of of temp.
     *
     * @param id id
     * @return of temp result
     */
    public static NStoreKey ofTemp(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.TEMP).id(id n store type.temp).id(id
         * @return of result
         */
        return of(NStoreType.TEMP).id(id);
    }

    /**
     * Creates a new instance of of lib.
     *
     * @param id id
     * @return of lib result
     */
    public static NStoreKey ofLib(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param NStoreType.LIB).id(id n store type.lib).id(id
         * @return of result
         */
        return of(NStoreType.LIB).id(id);
    }

    /**
     * Creates a new instance of of.
     *
     * @param storeType store type
     * @return of result
     */
    public static NStoreKey of(NStoreType storeType) {
        return new NStoreKey(NStoreScope.WORKSPACE, storeType == null ? NStoreType.CONF : storeType, null, null, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param id id
     * @return of result
     */
    public static NStoreKey of(NId id) {
        return new NStoreKey(NStoreScope.WORKSPACE, NStoreType.CONF, id, null, null);
    }

    /**
     * Creates a new instance of of shared.
     *
     * @param id id
     * @return of shared result
     */
    public static NStoreKey ofShared(NId id) {
        return new NStoreKey(NStoreScope.WORKSPACE, NStoreType.CONF, id == null ? null : id.sharedId(), null, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param storeScope store scope
     * @param storeType store type
     * @param id id
     * @param name name
     * @return of result
     */
    public static NStoreKey of(NStoreScope storeScope, NStoreType storeType, NId id, String name) {
        return new NStoreKey(storeScope, storeType, id, null, name);
    }

    /**
     * Creates a new instance of of.
     *
     * @param storeScope store scope
     * @param storeType store type
     * @param id id
     * @param repoUuid repo uuid
     * @param name name
     * @return of result
     */
    public static NStoreKey of(NStoreScope storeScope, NStoreType storeType, NId id, String repoUuid, String name) {
        return new NStoreKey(storeScope, storeType, id, repoUuid, name);
    }

    /**
     * @since 1.0.0
     * @param storeScope user or system scope
     * @param storeType storeType (conf, bin, etc)
     * @param id application id
     * @return NStoreKey instance
     */
    public static NStoreKey of(NStoreScope storeScope, NStoreType storeType, NId id) {
        return new NStoreKey(storeScope, storeType, id, null, null);
    }

    /**
     * Creates a new instance of of cache.
     *
     * @param id id
     * @param repoUuid repo uuid
     * @param name name
     * @return of cache result
     */
    public static NStoreKey ofCache(NId id, String repoUuid, String name) {
        return new NStoreKey(NStoreScope.WORKSPACE, NStoreType.CACHE, id, repoUuid, name);
    }

    /**
     * Creates a new instance of of cache faced.
     *
     * @param id id
     * @param repoUuid repo uuid
     * @param faceName face name
     * @return of cache faced result
     */
    public static NStoreKey ofCacheFaced(NId id, String repoUuid, String faceName) {
        /**
         * Creates a new instance of of faced.
         *
         * @param NStoreType.CACHE n store type.cache
         * @param id id
         * @param repoUuid repo uuid
         * @param faceName face name
         * @return of faced result
         */
        return ofFaced(NStoreType.CACHE, id, repoUuid, faceName);
    }

    /**
     * Creates a new instance of of faced.
     *
     * @param storeType store type
     * @param id id
     * @param repoUuid repo uuid
     * @param faceName face name
     * @return of faced result
     */
    public static NStoreKey ofFaced(NStoreType storeType, NId id, String repoUuid, String faceName) {
        return new NStoreKey(NStoreScope.WORKSPACE, storeType, id, repoUuid, NWorkspace.of().getDefaultIdFilename(id.builder().face(faceName).build()));
    }

    /**
     * Creates a new instance of of conf.
     *
     * @param id id
     * @param repoUuid repo uuid
     * @param name name
     * @return of conf result
     */
    public static NStoreKey ofConf(NId id, String repoUuid, String name) {
        return new NStoreKey(NStoreScope.WORKSPACE, NStoreType.CONF, id, repoUuid, name);
    }

    /**
     * Creates a new instance of of conf faced.
     *
     * @param id id
     * @param repoUuid repo uuid
     * @param faceName face name
     * @return of conf faced result
     */
    public static NStoreKey ofConfFaced(NId id, String repoUuid, String faceName) {
        /**
         * Creates a new instance of of faced.
         *
         * @param NStoreType.CONF n store type.conf
         * @param id id
         * @param repoUuid repo uuid
         * @param faceName face name
         * @return of faced result
         */
        return ofFaced(NStoreType.CONF, id, repoUuid, faceName);
    }


    /**
     * N store key.
     *
     * @param storeScope store scope
     * @param storeType store type
     * @param id id
     * @param repoUuid repo uuid
     * @param name name
     * @return n store key result
     */
    public NStoreKey(NStoreScope storeScope, NStoreType storeType, NId id, String repoUuid, String name) {
        if (NBlankable.isBlank(name)) {
            this.name = null;
        } else {
            NAssert.requireNamedTrue(name.matches("[a-zA-Z0-9._-]+"), "name matches [a-zA-Z0-9._-]+");
            this.name = name;
        }
        this.id = NBlankable.isBlank(id) ? null : id;
        this.storeScope = NAssert.requireNamedNonNull(storeScope, "storeScope");
        this.storeType = NAssert.requireNamedNonNull(storeType, "storeType");
        if (NBlankable.isBlank(repoUuid)) {
            this.repoUuid = null;
        } else {
            NAssert.requireNamedTrue(repoUuid.matches("[a-zA-Z0-9._-]+"), "repoUuid matches [a-zA-Z0-9._-]+");
            this.repoUuid = repoUuid;
        }
    }


    /**
     * Name.
     *
     * @return name result
     */
    public String name() {
        return name;
    }


    /**
     * Id.
     *
     * @param id id
     * @return id result
     */
    public NStoreKey id(NId id) {
        return new NStoreKey(storeScope, storeType, id, repoUuid, name);
    }

    /**
     * Shared id.
     *
     * @param id id
     * @return shared id result
     */
    public NStoreKey sharedId(NId id) {
        return new NStoreKey(storeScope, storeType, id == null ? null : id.sharedId(), repoUuid, name);
    }

    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    public NStoreKey name(String name) {
        return new NStoreKey(storeScope, storeType, id, repoUuid, name);
    }

    /**
     * Repo.
     *
     * @param repo repo
     * @return repo result
     */
    public NStoreKey repo(String repo) {
        return new NStoreKey(storeScope, storeType, id, repo, name);
    }

    /**
     * System.
     *
     * @return system result
     */
    public NStoreKey system() {
        /**
         * Scope.
         *
         * @param NStoreScope.SYSTEM n store scope.system
         * @return scope result
         */
        return scope(NStoreScope.SYSTEM);
    }

    /**
     * User.
     *
     * @return user result
     */
    public NStoreKey user() {
        /**
         * Scope.
         *
         * @param NStoreScope.USER n store scope.user
         * @return scope result
         */
        return scope(NStoreScope.USER);
    }

    /**
     * Workspace.
     *
     * @return workspace result
     */
    public NStoreKey workspace() {
        /**
         * Scope.
         *
         * @param NStoreScope.WORKSPACE n store scope.workspace
         * @return scope result
         */
        return scope(NStoreScope.WORKSPACE);
    }

    /**
     * Scope.
     *
     * @param scope scope
     * @return scope result
     */
    public NStoreKey scope(NStoreScope scope) {
        return new NStoreKey(scope != null ? scope : storeScope, storeType, id, repoUuid, name);
    }

    /**
     * Type.
     *
     * @param type type
     * @return type result
     */
    public NStoreKey type(NStoreType type) {
        return new NStoreKey(storeScope, type != null ? type : storeType, id, repoUuid, name);
    }

    /**
     * Lib.
     *
     * @return lib result
     */
    public NStoreKey lib() {
        /**
         * Type.
         *
         * @param NStoreType.LIB n store type.lib
         * @return type result
         */
        return type(NStoreType.LIB);
    }

    /**
     * Bin.
     *
     * @return bin result
     */
    public NStoreKey bin() {
        /**
         * Type.
         *
         * @param NStoreType.BIN n store type.bin
         * @return type result
         */
        return type(NStoreType.BIN);
    }

    /**
     * Run.
     *
     * @return run result
     */
    public NStoreKey run() {
        /**
         * Type.
         *
         * @param NStoreType.RUN n store type.run
         * @return type result
         */
        return type(NStoreType.RUN);
    }

    /**
     * Conf.
     *
     * @return conf result
     */
    public NStoreKey conf() {
        /**
         * Type.
         *
         * @param NStoreType.CONF n store type.conf
         * @return type result
         */
        return type(NStoreType.CONF);
    }

    /**
     * Log.
     *
     * @return log result
     */
    public NStoreKey log() {
        /**
         * Type.
         *
         * @param NStoreType.LOG n store type.log
         * @return type result
         */
        return type(NStoreType.LOG);
    }

    /**
     * Cache.
     *
     * @return cache result
     */
    public NStoreKey cache() {
        /**
         * Type.
         *
         * @param NStoreType.CACHE n store type.cache
         * @return type result
         */
        return type(NStoreType.CACHE);
    }

    /**
     * Temp.
     *
     * @return temp result
     */
    public NStoreKey temp() {
        /**
         * Type.
         *
         * @param NStoreType.TEMP n store type.temp
         * @return type result
         */
        return type(NStoreType.TEMP);
    }

    /**
     * Var.
     *
     * @return var result
     */
    public NStoreKey var() {
        /**
         * Type.
         *
         * @param NStoreType.VAR n store type.var
         * @return type result
         */
        return type(NStoreType.VAR);
    }

    /**
     * Id.
     *
     * @return id result
     */
    public NId id() {
        return id;
    }

    /**
     * Repo.
     *
     * @return repo result
     */
    public String repo() {
        return repoUuid;
    }

    /**
     * Type.
     *
     * @return type result
     */
    public NStoreType type() {
        return storeType;
    }

    /**
     * Scope.
     *
     * @return scope result
     */
    public NStoreScope scope() {
        return storeScope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NStoreKey that = (NStoreKey) o;
        return Objects.equals(name, that.name) && Objects.equals(id, that.id) && Objects.equals(repoUuid, that.repoUuid) && storeType == that.storeType && storeScope == that.storeScope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, repoUuid, storeType, storeScope);
    }

    @Override
    public String toString() {
        return "NStoreKey{" +
                "name=" + NStringUtils.formatStringLiteral(name) +
                ", id=" + id +
                ", repoUuid=" + NStringUtils.formatStringLiteral(repoUuid) +
                ", storeType=" + storeType +
                ", storeScope=" + storeScope +
                '}';
    }
}
