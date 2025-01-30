package net.thevpc.nuts;

import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Objects;

public class NLocationKey {
    private String name;
    private NId id;
    private String repoUuid;
    private NStoreType storeType;

    public static NLocationKey of(NStoreType storeType, NId id, String name) {
        return new NLocationKey(storeType, id, null, name);
    }

    public static NLocationKey of(NStoreType storeType, NId id, String repoUuid, String name) {
        return new NLocationKey(storeType, id, repoUuid, name);
    }

    public static NLocationKey ofCache(NId id, String repoUuid, String name) {
        return new NLocationKey(NStoreType.CACHE, id, repoUuid, name);
    }

    public static NLocationKey ofCacheFaced(NId id, String repoUuid, String faceName) {
        return ofFaced(NStoreType.CACHE, id, repoUuid, faceName);
    }

    public static NLocationKey ofFaced(NStoreType storeType, NId id, String repoUuid, String faceName) {
        return new NLocationKey(storeType, id, repoUuid, NWorkspace.of().getDefaultIdFilename(id.builder().setFace(faceName).build()));
    }

    public static NLocationKey ofConf(NId id, String repoUuid, String name) {
        return new NLocationKey(NStoreType.CONF, id, repoUuid, name);
    }

    public static NLocationKey ofConfFaced(NId id, String repoUuid, String faceName) {
        return ofFaced(NStoreType.CONF, id, repoUuid, faceName);
    }


    public NLocationKey(NStoreType storeType, NId id, String repoUuid, String name) {
        if (NBlankable.isBlank(name)) {
            this.name = null;
        } else {
            NAssert.requireTrue(name.matches("[a-zA-Z0-9._-]+"), "name matches [a-zA-Z0-9._-]+");
            this.name = name;
        }
        this.id = NAssert.requireNonBlank(id, "id");
        this.storeType = NAssert.requireNonNull(storeType, "storeType");
        this.repoUuid = NStringUtils.trimToNull(repoUuid);
    }


    public String getName() {
        return name;
    }

    public NId getId() {
        return id;
    }

    public String getRepoUuid() {
        return repoUuid;
    }

    public NStoreType getStoreType() {
        return storeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NLocationKey that = (NLocationKey) o;
        return Objects.equals(name, that.name) && Objects.equals(id, that.id) && Objects.equals(repoUuid, that.repoUuid) && storeType == that.storeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, repoUuid, storeType);
    }

    @Override
    public String toString() {
        return "NLocationKey{" +
                "name=" + NStringUtils.formatStringLiteral(name) +
                ", id=" + id +
                ", repoUuid=" + NStringUtils.formatStringLiteral(repoUuid) +
                ", storeType=" + storeType +
                '}';
    }
}
