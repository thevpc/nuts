package net.thevpc.nuts.app;

import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NToStringBuilder;
import net.thevpc.nuts.util.NUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents initialization information for an application.
 * This class holds various configuration details needed during
 * the initialization phase of an application, including arguments,
 * application class, store ID, start time, and store location resolver.
 */
public class NAppInitInfo {
    private final String[] args;
    private final Class<?> sourceType;
    private final Object source;
    private final NApplicationHandler handler;
    private final NClock startTime;
    private final NAppStoreLocationResolver storeLocationSupplier;


    /**
     * Initializes an instance of {@code NAppInitInfo} with the specified arguments, application class,
     * store ID, and start time.
     *
     * @param args       The command-line arguments passed to the application.
     * @param sourceType The application's main class.
     * @param startTime  The start time of the application, represented as an {@code NClock} instance.
     */
    public NAppInitInfo(String[] args, Class<?> sourceType, Object source, NApplicationHandler handler, NAppStoreLocationResolver storeLocationSupplier, NClock startTime) {
        this.args = NUtils.firstNonNullLazy(args, () -> new String[0]);
        this.sourceType = sourceType;
        this.source = source;
        this.handler = handler;
        this.startTime = startTime;
        this.storeLocationSupplier = storeLocationSupplier;
    }

    /**
     * Retrieves the command-line arguments passed to the application.
     *
     * @return An array of strings representing the command-line arguments
     */
    @NGetter
    public String[] args() {
        return Arrays.copyOf(args, args.length);
    }


    /**
     * Retrieves the application's main class.
     *
     * @return The application's main class, represented as a {@code Class<?>} object.
     */
    @NGetter
    public Class<?> sourceType() {
        return sourceType;
    }


    /**
     * Start time.
     *
     * @return start time result
     */
    @NGetter
    public NClock startTime() {
        return startTime;
    }


    /**
     * Store location supplier.
     *
     * @return store location supplier result
     */
    @NGetter
    public NAppStoreLocationResolver storeLocationSupplier() {
        return storeLocationSupplier;
    }

    /**
     * Application.
     *
     * @return application result
     */
    @NGetter
    public NApplicationHandler handler() {
        return handler;
    }

    /**
     * Source.
     *
     * @return source result
     */
    @NGetter
    public Object source() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NAppInitInfo that = (NAppInitInfo) o;
        return Objects.deepEquals(args, that.args) && Objects.equals(sourceType, that.sourceType) && Objects.equals(source, that.source) && Objects.equals(handler, that.handler) && Objects.equals(startTime, that.startTime) && Objects.equals(storeLocationSupplier, that.storeLocationSupplier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(args), sourceType, source, handler, startTime, storeLocationSupplier);
    }

    @Override
    public String toString() {
        return NToStringBuilder.of(this).omitBlanks(true)
                .add("args", args)
                .add("source", source)
                .add("handler", handler)
                .add("startTime", startTime)
                .add("storeLocationSupplier", storeLocationSupplier)
                .build();
    }
}
