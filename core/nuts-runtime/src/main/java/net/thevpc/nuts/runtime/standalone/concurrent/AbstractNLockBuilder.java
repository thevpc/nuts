package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NLockBuilder;
import net.thevpc.nuts.concurrent.NLockStore;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.time.NDuration;

public abstract class AbstractNLockBuilder implements NLockBuilder {
    private String id;
    private Object target;
    private NPath lockFile;
    private boolean companion;
    private String companionNameOrSuffix;
    private NDuration leaseDuration;
    private NDuration timeout;
    private NDuration retryInterval;
    private boolean autoRenew;
    private NLockStore store;

    public AbstractNLockBuilder() {
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public NLockBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Object target() {
        return target;
    }

    @Override
    public NLockBuilder target(Object target) {
        this.target = target;
        return this;
    }

    @Override
    public NLockBuilder lockFile(NPath lockFile) {
        this.lockFile = lockFile;
        this.companion = false;
        return this;
    }

    @Override
    public NPath lockFile() {
        return lockFile;
    }

    @Override
    public NLockBuilder companion(NPath targetPath) {
        return companion(targetPath, null);
    }

    @Override
    public NLockBuilder companion(NPath targetPath, String suffixOrFileName) {
        this.target = targetPath;
        this.companion = true;
        this.companionNameOrSuffix = suffixOrFileName;
        return this;
    }

    @Override
    public boolean isCompanion() {
        return companion;
    }

    @Override
    public String companionNameOrSuffix() {
        return companionNameOrSuffix;
    }

    @Override
    public NLockBuilder leaseDuration(NDuration leaseDuration) {
        this.leaseDuration = leaseDuration;
        return this;
    }

    @Override
    public NDuration leaseDuration() {
        return leaseDuration;
    }

    @Override
    public NLockBuilder timeout(NDuration timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public NDuration timeout() {
        return timeout;
    }

    @Override
    public NLockBuilder retryInterval(NDuration retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }

    @Override
    public NDuration retryInterval() {
        return retryInterval;
    }

    @Override
    public NLockBuilder autoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
        return this;
    }

    @Override
    public boolean isAutoRenew() {
        return autoRenew;
    }

    @Override
    public NLockBuilder store(NLockStore store) {
        this.store = store;
        return this;
    }

    @Override
    public NLockStore store() {
        return store;
    }
}
