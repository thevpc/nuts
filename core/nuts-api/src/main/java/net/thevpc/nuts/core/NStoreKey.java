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
     * Creates a new instance of workspace.
     *
     * @return of workspace result
     */
    public static NStoreKey ofWorkspace() {
        return of(NStoreScope.WORKSPACE);
    }

    /**
     * Creates a new instance of system.
     *
     * @return of system result
     */
    public static NStoreKey ofSystem() {
        return of(NStoreScope.SYSTEM);
    }

    /**
     * Creates a new instance of user.
     *
     * @return of user result
     */
    public static NStoreKey ofUser() {
        return of(NStoreScope.USER);
    }

    /**
     * Creates a new instance of shared workspace.
     *
     * @param id id
     * @return of shared workspace result
     */
    public static NStoreKey ofSharedWorkspace(NId id) {
        return of(NStoreScope.WORKSPACE).sharedId(id);
    }

    /**
     * Creates a new instance of shared user.
     *
     * @param id id
     * @return of shared user result
     */
    public static NStoreKey ofSharedUser(NId id) {
        return of(NStoreScope.USER).sharedId(id);
    }

    /**
     * Creates a new instance of shared system.
     *
     * @param id id
     * @return of shared system result
     */
    public static NStoreKey ofSharedSystem(NId id) {
        return of(NStoreScope.SYSTEM).sharedId(id);
    }

    /**
     * Creates a new instance of workspace.
     *
     * @param id id
     * @return of workspace result
     */
    public static NStoreKey ofWorkspace(NId id) {
        return of(NStoreScope.WORKSPACE).id(id);
    }

    /**
     * Creates a new instance of system.
     *
     * @param id id
     * @return of system result
     */
    public static NStoreKey ofSystem(NId id) {
        return of(NStoreScope.SYSTEM).id(id);
    }

    /**
     * Creates a new instance of user.
     *
     * @param id id
     * @return of user result
     */
    public static NStoreKey ofUser(NId id) {
        return of(NStoreScope.USER).id(id);
    }

    /**
     * Creates a new instance of user.
     *
     * @param storeType store type
     * @return of user result
     */
    public static NStoreKey ofUser(NStoreType storeType) {
        return of(NStoreScope.USER).type(storeType);
    }

    /**
     * Creates a new instance of system.
     *
     * @param storeType store type
     * @return of system result
     */
    public static NStoreKey ofSystem(NStoreType storeType) {
        return of(NStoreScope.SYSTEM).type(storeType);
    }

    /**
     * Creates a new instance of base.
     *
     * @param storeType store type
     * @return of base result
     */
    public static NStoreKey ofBase(NStoreType storeType) {
        return of(NStoreScope.BASE).type(storeType);
    }

    /**
     * Creates a new instance of workspace.
     *
     * @param storeType store type
     * @return of workspace result
     */
    public static NStoreKey ofWorkspace(NStoreType storeType) {
        return of(NStoreScope.WORKSPACE).type(storeType);
    }

    /**
     * Creates a new instance.
     *
     * @param storeScope store scope
     * @return of result
     */
    public static NStoreKey of(NStoreScope storeScope) {
        return new NStoreKey(storeScope == null ? NStoreScope.WORKSPACE : storeScope, NStoreType.CONF, null, null, null);
    }

    /**
     * Creates a new instance of conf.
     *
     * @return of conf result
     */
    public static NStoreKey ofConf() {
        return of(NStoreType.CONF);
    }

    /**
     * Creates a new instance of bin.
     *
     * @return of bin result
     */
    public static NStoreKey ofBin() {
        return of(NStoreType.BIN);
    }

    /**
     * Creates a new instance of cache.
     *
     * @return of cache result
     */
    public static NStoreKey ofCache() {
        return of(NStoreType.CACHE);
    }

    /**
     * Creates a new instance of var.
     *
     * @return of var result
     */
    public static NStoreKey ofVar() {
        return of(NStoreType.VAR);
    }

    /**
     * Creates a new instance of log.
     *
     * @return of log result
     */
    public static NStoreKey ofLog() {
        return of(NStoreType.LOG);
    }

    /**
     * Creates a new instance of run.
     *
     * @return of run result
     */
    public static NStoreKey ofRun() {
        return of(NStoreType.RUN);
    }

    /**
     * Creates a new instance of temp.
     *
     * @return of temp result
     */
    public static NStoreKey ofTemp() {
        return of(NStoreType.TEMP);
    }

    /**
     * Creates a new instance of lib.
     *
     * @return of lib result
     */
    public static NStoreKey ofLib() {
        return of(NStoreType.LIB);
    }

    /**
     * Creates a new instance of conf.
     *
     * @param id id
     * @return of conf result
     */
    public static NStoreKey ofConf(NId id) {
        return of(NStoreType.CONF).id(id);
    }

    /**
     * Creates a new instance of bin.
     *
     * @param id id
     * @return of bin result
     */
    public static NStoreKey ofBin(NId id) {
        return of(NStoreType.BIN).id(id);
    }

    /**
     * Creates a new instance of cache.
     *
     * @param id id
     * @return of cache result
     */
    public static NStoreKey ofCache(NId id) {
        return of(NStoreType.CACHE).id(id);
    }

    /**
     * Creates a new instance of var.
     *
     * @param id id
     * @return of var result
     */
    public static NStoreKey ofVar(NId id) {
        return of(NStoreType.VAR).id(id);
    }

    /**
     * Creates a new instance of log.
     *
     * @param id id
     * @return of log result
     */
    public static NStoreKey ofLog(NId id) {
        return of(NStoreType.LOG).id(id);
    }

    /**
     * Creates a new instance of run.
     *
     * @param id id
     * @return of run result
     */
    public static NStoreKey ofRun(NId id) {
        return of(NStoreType.RUN).id(id);
    }

    /**
     * Creates a new instance of temp.
     *
     * @param id id
     * @return of temp result
     */
    public static NStoreKey ofTemp(NId id) {
        return of(NStoreType.TEMP).id(id);
    }

    /**
     * Creates a new instance of lib.
     *
     * @param id id
     * @return of lib result
     */
    public static NStoreKey ofLib(NId id) {
        return of(NStoreType.LIB).id(id);
    }

    /**
     * Creates a new instance.
     *
     * @param storeType store type
     * @return of result
     */
    public static NStoreKey of(NStoreType storeType) {
        return new NStoreKey(NStoreScope.WORKSPACE, storeType == null ? NStoreType.CONF : storeType, null, null, null);
    }

    /**
     * Creates a new instance.
     *
     * @param id id
     * @return of result
     */
    public static NStoreKey of(NId id) {
        return new NStoreKey(NStoreScope.WORKSPACE, NStoreType.CONF, id, null, null);
    }

    /**
     * Creates a new instance of shared.
     *
     * @param id id
     * @return of shared result
     */
    public static NStoreKey ofShared(NId id) {
        return new NStoreKey(NStoreScope.WORKSPACE, NStoreType.CONF, id == null ? null : id.sharedId(), null, null);
    }

    /**
     * Creates a new instance.
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
     * Creates a new instance.
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
     * Creates a new instance of cache.
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
     * Creates a new instance of cache faced.
     *
     * @param id id
     * @param repoUuid repo uuid
     * @param faceName face name
     * @return of cache faced result
     */
    public static NStoreKey ofCacheFaced(NId id, String repoUuid, String faceName) {
        return ofFaced(NStoreType.CACHE, id, repoUuid, faceName);
    }

    /**
     * Creates a new instance of faced.
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
     * Creates a new instance of conf.
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
     * Creates a new instance of conf faced.
     *
     * @param id id
     * @param repoUuid repo uuid
     * @param faceName face name
     * @return of conf faced result
     */
    public static NStoreKey ofConfFaced(NId id, String repoUuid, String faceName) {
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
        return scope(NStoreScope.SYSTEM);
    }

    /**
     * User.
     *
     * @return user result
     */
    public NStoreKey user() {
        return scope(NStoreScope.USER);
    }

    /**
     * Workspace.
     *
     * @return workspace result
     */
    public NStoreKey workspace() {
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
        return type(NStoreType.LIB);
    }

    /**
     * Bin.
     *
     * @return bin result
     */
    public NStoreKey bin() {
        return type(NStoreType.BIN);
    }

    /**
     * Run.
     *
     * @return run result
     */
    public NStoreKey run() {
        return type(NStoreType.RUN);
    }

    /**
     * Conf.
     *
     * @return conf result
     */
    public NStoreKey conf() {
        return type(NStoreType.CONF);
    }

    /**
     * Log.
     *
     * @return log result
     */
    public NStoreKey log() {
        return type(NStoreType.LOG);
    }

    /**
     * Cache.
     *
     * @return cache result
     */
    public NStoreKey cache() {
        return type(NStoreType.CACHE);
    }

    /**
     * Temp.
     *
     * @return temp result
     */
    public NStoreKey temp() {
        return type(NStoreType.TEMP);
    }

    /**
     * Var.
     *
     * @return var result
     */
    public NStoreKey var() {
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
