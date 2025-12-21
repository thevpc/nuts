package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.log.NLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NExtensionTypeInfoPool {
    private Map<ApiAndImpl, NExtensionTypeInfo> map=new ConcurrentHashMap<>();
    public final HashMap<String, String> _alreadyLogger = new HashMap<>();
    public final NLog logger;
    public final NBeanCache beanCache;

    public NExtensionTypeInfoPool(NLog logger, NBeanCache beanCache) {
        this.logger = logger;
        this.beanCache= beanCache;;
    }

    public <T> NExtensionTypeInfo<T> get(Class<? extends T> implType, Class<T> apiType) {
        return (NExtensionTypeInfo<T>) map.computeIfAbsent(new ApiAndImpl(implType,apiType), k -> new NExtensionTypeInfo(implType,apiType, NExtensionTypeInfoPool.this,beanCache));
    }


    private static class ApiAndImpl{
        Class apiType;
        Class implType;

        public ApiAndImpl(Class implType,Class apiType) {
            this.apiType = apiType;
            this.implType = implType;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ApiAndImpl apiAnd = (ApiAndImpl) o;
            return Objects.equals(apiType, apiAnd.apiType) && Objects.equals(implType, apiAnd.implType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(apiType, implType);
        }
    }

}
