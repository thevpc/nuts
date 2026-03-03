package net.thevpc.nuts.app;

import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.util.NUtils;

import java.util.Arrays;

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
    private final NApplication appInstance;
    private final NClock startTime;
    private final NAppStoreLocationResolver storeLocationSupplier;


    /**
     * Initializes an instance of {@code NAppInitInfo} with the specified arguments, application class,
     * store ID, and start time.
     *
     * @param args      The command-line arguments passed to the application.
     * @param sourceType  The application's main class.
     * @param startTime The start time of the application, represented as an {@code NClock} instance.
     */
    public NAppInitInfo(String[] args, Class<?> sourceType, Object source, NApplication appInstance, NAppStoreLocationResolver storeLocationSupplier, NClock startTime) {
        this.args = NUtils.firstNonNullLazy(args,()->new String[0]);
        this.sourceType = sourceType;
        this.source = source;
        this.appInstance = appInstance;
        this.startTime = startTime;
        this.storeLocationSupplier = storeLocationSupplier;
    }

    /**
     * Retrieves the command-line arguments passed to the application.
     *
     * @return An array of strings representing the command-line arguments
     */
    public String[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }


    /**
     * Retrieves the application's main class.
     *
     * @return The application's main class, represented as a {@code Class<?>} object.
     */
    public Class<?> getSourceType() {
        return sourceType;
    }


    public NClock getStartTime() {
        return startTime;
    }


    public NAppStoreLocationResolver getStoreLocationSupplier() {
        return storeLocationSupplier;
    }

    public NApplication getApplication() {
        return appInstance;
    }

    public Object getSource() {
        return source;
    }
}
