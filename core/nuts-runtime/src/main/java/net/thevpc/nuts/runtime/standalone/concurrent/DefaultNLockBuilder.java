package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NLock;
import net.thevpc.nuts.concurrent.NLockException;
import net.thevpc.nuts.concurrent.NLockStore;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.text.NMsg;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNLockBuilder extends AbstractNLockBuilder {
    private final Map<Object, DefaultMemLock> memLocks = new HashMap<>();

    public DefaultNLockBuilder() {
        super();
    }

    @Override
    public NLock build() {
        NLockStore st = store();
        if (st != null) {
            String lockId = id();
            if (lockId == null || lockId.trim().isEmpty()) {
                if (target() != null) {
                    lockId = target().toString();
                } else {
                    lockId = UUID.randomUUID().toString();
                }
            }
            return new NStoreNLock(st, lockId, null, leaseDuration(), timeout(), retryInterval(), isAutoRenew());
        }

        NPath lf = lockFile();
        if (lf != null) {
            Path p = toPath(lf);
            if (p != null) {
                return new DefaultFileNLock(p, target() != null ? target() : lf);
            }
            return memLocks.computeIfAbsent(lf, e -> new DefaultMemLock(e));
        }

        Object tgt = target();
        if (isCompanion()) {
            NPath tgtPath = toNPath(tgt);
            if (tgtPath == null) {
                throw new NLockException(NMsg.ofP("companion lock requires a path target"), null, tgt);
            }
            NPath compPath = resolveCompanion(tgtPath, companionNameOrSuffix());
            return new DefaultFileNLock(compPath.toPath().get(), tgt);
        }

        if (tgt != null) {
            Path p = toPath(tgt);
            if (p != null) {
                NPath tgtPath = NPath.of(p);
                NPath compPath = resolveCompanion(tgtPath, null);
                return new DefaultFileNLock(compPath.toPath().get(), tgt);
            }
            return memLocks.computeIfAbsent(tgt, e -> new DefaultMemLock(e));
        }

        String lid = id();
        if (lid != null && !lid.trim().isEmpty()) {
            return memLocks.computeIfAbsent(lid, e -> new DefaultMemLock(e));
        }

        throw new NLockException(NMsg.ofP("unsupported lock for null"), null, null);
    }

    private NPath resolveCompanion(NPath target, String suffixOrName) {
        if (suffixOrName != null && !suffixOrName.trim().isEmpty()) {
            if (suffixOrName.startsWith(".")) {
                return target.resolveSibling(target.name() + suffixOrName);
            }
            return target.isDirectory() ? target.resolve(suffixOrName) : target.resolveSibling(suffixOrName);
        }
        if (target.isDirectory()) {
            return target.resolve(".nuts-lock");
        }
        return target.resolveSibling(target.name() + ".lock");
    }

    private NPath toNPath(Object obj) {
        if (obj instanceof NPath) {
            return (NPath) obj;
        } else if (obj instanceof Path) {
            return NPath.of((Path) obj);
        } else if (obj instanceof File) {
            return NPath.of((File) obj);
        }
        return null;
    }

    private Path toPath(Object lockedObject) {
        if (lockedObject instanceof Path) {
            return (Path) lockedObject;
        } else if (lockedObject instanceof NPath) {
            return ((NPath) lockedObject).toPath().get();
        } else if (lockedObject instanceof File) {
            return ((File) lockedObject).toPath();
        }
        return null;
    }
}
