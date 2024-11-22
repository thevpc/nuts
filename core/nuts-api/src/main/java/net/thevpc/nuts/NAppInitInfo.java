package net.thevpc.nuts;

import net.thevpc.nuts.time.NClock;

public class NAppInitInfo {
    private String[] args;
    private Class<?> appClass;
    private String storeId;
    private NClock startTime;
    private NAppStoreLocationResolver storeLocationSupplier;

    public NAppInitInfo() {
    }

    public NAppInitInfo(String[] args, Class<?> appClass, String storeId, NClock startTime) {
        this.args = args;
        this.appClass = appClass;
        this.storeId = storeId;
        this.startTime = startTime;
    }

    public String[] getArgs() {
        return args;
    }

    public NAppInitInfo setArgs(String[] args) {
        this.args = args;
        return this;
    }

    public Class<?> getAppClass() {
        return appClass;
    }

    public NAppInitInfo setAppClass(Class<?> appClass) {
        this.appClass = appClass;
        return this;
    }

    public String getStoreId() {
        return storeId;
    }

    public NAppInitInfo setStoreId(String storeId) {
        this.storeId = storeId;
        return this;
    }

    public NClock getStartTime() {
        return startTime;
    }

    public NAppInitInfo setStartTime(NClock startTime) {
        this.startTime = startTime;
        return this;
    }

    public NAppStoreLocationResolver getStoreLocationSupplier() {
        return storeLocationSupplier;
    }

    public NAppInitInfo setStoreLocationSupplier(NAppStoreLocationResolver storeLocationSupplier) {
        this.storeLocationSupplier = storeLocationSupplier;
        return this;
    }
}
