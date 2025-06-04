package net.thevpc.nuts.runtime.standalone.io.cache;


import net.thevpc.nuts.NWorkspace;

import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NLocationKey;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NBlankable;

import java.util.Objects;
import java.util.function.Supplier;

public class DefaultCachedSupplier<T> implements CachedSupplier<T> {
    private final Supplier<T> supplier;
    private T value;
    private String valueCacheId;
    private boolean evaluated;
    private RuntimeException exception;
    private final Class<T> clazz;
    private final CacheValidator<T> validator;
    private NPath cachePath;
    private NPath cacheIdPath;
    private NCacheLevel level;
    private NLocationKey key;


    public static <T> DefaultCachedSupplier<T> ofMem(Supplier<T> supplier, CacheValidator<T> validator) {
        return new DefaultCachedSupplier<>(
                NCacheLevel.MEM,
                (Class) Object.class,
                null, supplier, validator
        );
    }

    public static <T> DefaultCachedSupplier<T> ofNone(Supplier<T> supplier) {
        return new DefaultCachedSupplier<>(
                NCacheLevel.NONE,
                (Class) Object.class,
                null, supplier, null
        );
    }

    public static <T> DefaultCachedSupplier<T> ofStore(Class<T> clazz,
                                                       NLocationKey key, Supplier<T> supplier, CacheValidator<T> validator) {
        return new DefaultCachedSupplier<>(NCacheLevel.STORE,
                clazz,
                key, supplier, validator
        );
    }

    public static <T> DefaultCachedSupplier<T> of(NCacheLevel level,
                                                  Class<T> clazz,
                                                  NLocationKey key, Supplier<T> supplier, CacheValidator<T> validator) {
        return new DefaultCachedSupplier<>(level,
                clazz,
                key, supplier, validator
        );
    }

    public DefaultCachedSupplier(NCacheLevel level,
                                 Class<T> clazz,
                                 NLocationKey key, Supplier<T> supplier, CacheValidator<T> validator) {
        this.level = level;
        this.key = key;
        this.clazz = clazz;
        this.validator = validator;
        this.supplier = supplier;
        if (this.level.ordinal() >= NCacheLevel.STORE.ordinal()) {
            if (key.getStoreType() != NStoreType.CACHE) {
                throw new IllegalArgumentException("expected cache store");
            }
            NWorkspace ws = NWorkspace.of();
            this.cachePath = ws.getStoreLocation(key.getId(), key.getStoreType(), key.getRepoUuid())
                    .resolve(ws.getDefaultIdFilename(key.getId().builder().setFace(key.getName() + ".value.cache").build()));
            this.cacheIdPath = ws.getStoreLocation(key.getId(), key.getStoreType(), key.getRepoUuid())
                    .resolve(ws.getDefaultIdFilename(key.getId().builder().setFace(key.getName() + ".id.cache").build()));
        }
    }

    @Override
    public T getValue() {
        return getValue(level);
    }

    @Override
    public T getValue(NCacheLevel level) {
        if (level == null) {
            level = this.level;
        }
        if (level.ordinal() > this.level.ordinal()) {
            level = this.level;
        }
        switch (level) {
            case NONE:
                return getNoCacheValue();
            case MEM:
                return getMemValue();
            case STORE:
                return getFileValue();
        }
        throw new IllegalArgumentException("unsupported");
    }

    private T getNoCacheValue() {
        return supplier.get();
    }


    private String loadCachedId() {
        try {
            if (cachePath.isRegularFile()) {
                return cacheIdPath.readString().trim();
            }
        } catch (Exception ex) {
            //just ignore
        }
        return null;
    }

    private T getFileValue() {
        String currentCacheId = null;
        if (validator != null) {
            currentCacheId = validator.getCacheId();
            if (!NBlankable.isBlank(currentCacheId) && !validator.isValidCacheId(valueCacheId)) {
                evaluated = false;
            }
        }
        if (!evaluated) {
            //check file
            if (currentCacheId != null) {
                String loadCacheId = loadCachedId();
                if (validator == null || validator.isValidCacheId(loadCacheId)) {
                    if (CoreIOUtils.isObsoletePath(cachePath)) {
                        //this is invalid cache!
                        if (cachePath.isRegularFile()) {
                            cachePath.delete();
                        }
                    } else {
                        try {
                            T d = NElementParser.ofJson().parse(cachePath, clazz);
                            if (d != null) {
                                if (validator != null && !validator.isValidValue(d)) {
                                    //this is invalid cache!
                                    if (cachePath.isRegularFile()) {
                                        cachePath.delete();
                                    }
                                }
                                this.valueCacheId = currentCacheId;
                                this.value = d;
                                return d;
                            }
                        } catch (Exception ex) {
                            //
                        }
                    }
                }
            }

            if (supplier == null) {
                value = null;
                valueCacheId = null;
                cachePath.delete();
                cacheIdPath.delete();
            } else {
                valueCacheId = null;
                value = null;
                try {
                    value = getNoCacheValue();
                    valueCacheId = currentCacheId;
                } catch (RuntimeException ex) {
                    exception = ex;
                } catch (Exception ex) {
                    exception = new RuntimeException(ex);
                }
                if (value != null) {
                    try {
                        NElementWriter.ofJson().write(value, cachePath);
                    } catch (Exception ex) {
                        //
                    }
                } else {
                    if (cachePath.isRegularFile()) {
                        cachePath.delete();
                    }
                }
            }
            evaluated = true;
        }
        if (exception != null) {
            throw exception;
        }
        return value;
    }

    private T getMemValue() {
        String currentCacheId = null;
        if (validator != null) {
            currentCacheId = validator.getCacheId();
            if (!NBlankable.isBlank(currentCacheId) || !validator.isValidCacheId(currentCacheId)) {
                evaluated = false;
            }
        }
        if (!evaluated) {
            if (supplier == null) {
                value = null;
                valueCacheId = null;
            } else {
                valueCacheId = null;
                try {
                    value = getNoCacheValue();
                    valueCacheId = currentCacheId;
                } catch (RuntimeException ex) {
                    exception = ex;
                } catch (Exception ex) {
                    exception = new RuntimeException(ex);
                }
            }
            evaluated = true;
        }
        if (exception != null) {
            throw exception;
        }
        return value;
    }

    public static class SimpleCacheValidator<T> implements CacheValidator<T> {

        public SimpleCacheValidator() {
        }

        @Override
        public String getCacheId() {
            return NWorkspaceExt.of().getModel().installationDigest;
        }

        @Override
        public boolean isValidCacheId(String cacheId) {
            String id = getCacheId();
            return NBlankable.isBlank(id) || Objects.equals(cacheId, id);
        }

        @Override
        public boolean isValidValue(T value) {
            return true;
        }
    }

    public interface CacheValidator<T> {
        String getCacheId();

        default boolean isValidCacheId(String cacheId) {
            String id = getCacheId();
            return NBlankable.isBlank(id) || Objects.equals(cacheId, id);
        }

        boolean isValidValue(T value);
    }


}
