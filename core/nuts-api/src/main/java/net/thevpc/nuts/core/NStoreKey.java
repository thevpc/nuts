package net.thevpc.nuts.core;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.platform.NStoreScope;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Objects;

public class NStoreKey {
    private final String name;
    private final NId id;
    private final String repoUuid;
    private final NStoreScope storeScope;
    private final NStoreType storeType;

    public static NStoreKey ofWorkspace() {
        return of(NStoreScope.WORKSPACE);
    }

    public static NStoreKey ofSystem() {
        return of(NStoreScope.SYSTEM);
    }

    public static NStoreKey ofUser() {
        return of(NStoreScope.USER);
    }

    public static NStoreKey ofSharedWorkspace(NId id) {
        return of(NStoreScope.WORKSPACE).sharedId(id);
    }

    public static NStoreKey ofSharedUser(NId id) {
        return of(NStoreScope.USER).sharedId(id);
    }

    public static NStoreKey ofSharedSystem(NId id) {
        return of(NStoreScope.SYSTEM).sharedId(id);
    }

    public static NStoreKey ofWorkspace(NId id) {
        return of(NStoreScope.WORKSPACE).id(id);
    }

    public static NStoreKey ofSystem(NId id) {
        return of(NStoreScope.SYSTEM).id(id);
    }

    public static NStoreKey ofUser(NId id) {
        return of(NStoreScope.USER).id(id);
    }

    public static NStoreKey ofUser(NStoreType storeType) {
        return of(NStoreScope.USER).type(storeType);
    }

    public static NStoreKey ofSystem(NStoreType storeType) {
        return of(NStoreScope.SYSTEM).type(storeType);
    }

    public static NStoreKey ofBase(NStoreType storeType) {
        return of(NStoreScope.BASE).type(storeType);
    }

    public static NStoreKey ofWorkspace(NStoreType storeType) {
        return of(NStoreScope.WORKSPACE).type(storeType);
    }

    public static NStoreKey of(NStoreScope storeScope) {
        return new NStoreKey(storeScope == null ? NStoreScope.WORKSPACE : storeScope, NStoreType.CONF, null, null, null);
    }

    public static NStoreKey ofConf() {
        return of(NStoreType.CONF);
    }

    public static NStoreKey ofBin() {
        return of(NStoreType.BIN);
    }

    public static NStoreKey ofCache() {
        return of(NStoreType.CACHE);
    }

    public static NStoreKey ofVar() {
        return of(NStoreType.VAR);
    }

    public static NStoreKey ofLog() {
        return of(NStoreType.LOG);
    }

    public static NStoreKey ofRun() {
        return of(NStoreType.RUN);
    }

    public static NStoreKey ofTemp() {
        return of(NStoreType.TEMP);
    }

    public static NStoreKey ofLib() {
        return of(NStoreType.LIB);
    }

    public static NStoreKey ofConf(NId id) {
        return of(NStoreType.CONF).id(id);
    }

    public static NStoreKey ofBin(NId id) {
        return of(NStoreType.BIN).id(id);
    }

    public static NStoreKey ofCache(NId id) {
        return of(NStoreType.CACHE).id(id);
    }

    public static NStoreKey ofVar(NId id) {
        return of(NStoreType.VAR).id(id);
    }

    public static NStoreKey ofLog(NId id) {
        return of(NStoreType.LOG).id(id);
    }

    public static NStoreKey ofRun(NId id) {
        return of(NStoreType.RUN).id(id);
    }

    public static NStoreKey ofTemp(NId id) {
        return of(NStoreType.TEMP).id(id);
    }

    public static NStoreKey ofLib(NId id) {
        return of(NStoreType.LIB).id(id);
    }

    public static NStoreKey of(NStoreType storeType) {
        return new NStoreKey(NStoreScope.WORKSPACE, storeType == null ? NStoreType.CONF : storeType, null, null, null);
    }

    public static NStoreKey of(NId id) {
        return new NStoreKey(NStoreScope.WORKSPACE, NStoreType.CONF, id, null, null);
    }

    public static NStoreKey ofShared(NId id) {
        return new NStoreKey(NStoreScope.WORKSPACE, NStoreType.CONF, id == null ? null : id.getSharedId(), null, null);
    }

    public static NStoreKey of(NStoreScope storeScope, NStoreType storeType, NId id, String name) {
        return new NStoreKey(storeScope, storeType, id, null, name);
    }

    public static NStoreKey of(NStoreScope storeScope, NStoreType storeType, NId id, String repoUuid, String name) {
        return new NStoreKey(storeScope, storeType, id, repoUuid, name);
    }

    public static NStoreKey ofCache(NId id, String repoUuid, String name) {
        return new NStoreKey(NStoreScope.WORKSPACE, NStoreType.CACHE, id, repoUuid, name);
    }

    public static NStoreKey ofCacheFaced(NId id, String repoUuid, String faceName) {
        return ofFaced(NStoreType.CACHE, id, repoUuid, faceName);
    }

    public static NStoreKey ofFaced(NStoreType storeType, NId id, String repoUuid, String faceName) {
        return new NStoreKey(NStoreScope.WORKSPACE, storeType, id, repoUuid, NWorkspace.of().getDefaultIdFilename(id.builder().setFace(faceName).build()));
    }

    public static NStoreKey ofConf(NId id, String repoUuid, String name) {
        return new NStoreKey(NStoreScope.WORKSPACE, NStoreType.CONF, id, repoUuid, name);
    }

    public static NStoreKey ofConfFaced(NId id, String repoUuid, String faceName) {
        return ofFaced(NStoreType.CONF, id, repoUuid, faceName);
    }


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


    public String name() {
        return name;
    }


    public NStoreKey id(NId id) {
        return new NStoreKey(storeScope, storeType, id, repoUuid, name);
    }

    public NStoreKey sharedId(NId id) {
        return new NStoreKey(storeScope, storeType, id == null ? null : id.getSharedId(), repoUuid, name);
    }

    public NStoreKey name(String name) {
        return new NStoreKey(storeScope, storeType, id, repoUuid, name);
    }

    public NStoreKey repo(String repo) {
        return new NStoreKey(storeScope, storeType, id, repo, name);
    }

    public NStoreKey system() {
        return scope(NStoreScope.SYSTEM);
    }

    public NStoreKey user() {
        return scope(NStoreScope.USER);
    }

    public NStoreKey workspace() {
        return scope(NStoreScope.WORKSPACE);
    }

    public NStoreKey scope(NStoreScope scope) {
        return new NStoreKey(scope != null ? scope : storeScope, storeType, id, repoUuid, name);
    }

    public NStoreKey type(NStoreType type) {
        return new NStoreKey(storeScope, type != null ? type : storeType, id, repoUuid, name);
    }

    public NStoreKey lib() {
        return type(NStoreType.LIB);
    }

    public NStoreKey bin() {
        return type(NStoreType.BIN);
    }

    public NStoreKey run() {
        return type(NStoreType.RUN);
    }

    public NStoreKey conf() {
        return type(NStoreType.CONF);
    }

    public NStoreKey log() {
        return type(NStoreType.LOG);
    }

    public NStoreKey cache() {
        return type(NStoreType.CACHE);
    }

    public NStoreKey temp() {
        return type(NStoreType.TEMP);
    }

    public NStoreKey var() {
        return type(NStoreType.VAR);
    }

    public NId id() {
        return id;
    }

    public String repo() {
        return repoUuid;
    }

    public NStoreType type() {
        return storeType;
    }

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
