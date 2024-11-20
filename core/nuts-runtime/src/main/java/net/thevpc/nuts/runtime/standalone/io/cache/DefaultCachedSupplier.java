package net.thevpc.nuts.runtime.standalone.io.cache;

import net.thevpc.nuts.NLocations;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.NLocationKey;
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
    private final NSession session;
    private final Class<T> clazz;
    private final CacheValidator<T> validator;
    private NElements elems;
    private NPath cachePath;
    private NPath cacheIdPath;
    private NCacheLevel level;
    private NLocationKey key;


    public static <T> DefaultCachedSupplier<T> ofMem(Supplier<T> supplier, CacheValidator<T> validator) {
        return new DefaultCachedSupplier<>(
                NCacheLevel.MEM,
                (Class) Object.class,
                null, supplier, validator, null
        );
    }

    public static <T> DefaultCachedSupplier<T> ofNone(Supplier<T> supplier) {
        return new DefaultCachedSupplier<>(
                NCacheLevel.NONE,
                (Class) Object.class,
                null, supplier, null, null
        );
    }

    public static <T> DefaultCachedSupplier<T> ofStore(Class<T> clazz,
                                                       NLocationKey key, Supplier<T> supplier, CacheValidator<T> validator, NSession session) {
        return new DefaultCachedSupplier<>(NCacheLevel.STORE,
                clazz,
                key, supplier, validator, session
        );
    }

    public static <T> DefaultCachedSupplier<T> of(NCacheLevel level,
                                                  Class<T> clazz,
                                                  NLocationKey key, Supplier<T> supplier, CacheValidator<T> validator, NSession session) {
        return new DefaultCachedSupplier<>(level,
                clazz,
                key, supplier, validator, session
        );
    }

    public DefaultCachedSupplier(NCacheLevel level,
                                 Class<T> clazz,
                                 NLocationKey key, Supplier<T> supplier, CacheValidator<T> validator, NSession session) {
        this.session = session;
        this.level = level;
        this.key = key;
        this.clazz = clazz;
        this.validator = validator;
        this.supplier = supplier;
        if (this.level.ordinal() >= NCacheLevel.STORE.ordinal()) {
            if (key.getStoreType() != NStoreType.CACHE) {
                throw new IllegalArgumentException("expected cache store");
            }
            NLocations nLocations = NLocations.of();
            this.elems = NElements.of();
            this.cachePath = nLocations.getStoreLocation(key.getId(), key.getStoreType(), key.getRepoUuid())
                    .resolve(nLocations.getDefaultIdFilename(key.getId().builder().setFace(key.getName() + ".value.cache").build()));
            this.cacheIdPath = nLocations.getStoreLocation(key.getId(), key.getStoreType(), key.getRepoUuid())
                    .resolve(nLocations.getDefaultIdFilename(key.getId().builder().setFace(key.getName() + ".id.cache").build()));
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
                        if(cachePath.isRegularFile()) {
                            cachePath.delete();
                        }
                    } else {
                        try {
                            T d = elems.json().parse(cachePath, clazz);
                            if (d != null) {
                                if (validator != null && !validator.isValidValue(d)) {
                                    //this is invalid cache!
                                    if(cachePath.isRegularFile()) {
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
                        elems.json().setValue(value).setNtf(false).print(cachePath);
                    } catch (Exception ex) {
                        //
                    }
                } else {
                    if(cachePath.isRegularFile()) {
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
        NSession session;

        public SimpleCacheValidator(NSession session) {
            this.session=session;
        }

        @Override
        public String getCacheId() {
            return NWorkspaceExt.of(session).getModel().installationDigest;
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
