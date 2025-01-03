package net.thevpc.nuts;

import net.thevpc.nuts.time.NClock;

/**
 * Represents initialization information for an application.
 * This class holds various configuration details needed during
 * the initialization phase of an application, including arguments,
 * application class, store ID, start time, and store location resolver.
 */
public class NAppInitInfo {
    private String[] args;
    private Class<?> appClass;
    private NClock startTime;
    private NAppStoreLocationResolver storeLocationSupplier;

    /**
     * Constructs a new instance of {@code NAppInitInfo}.
     * This is a default constructor that initializes the object
     * without setting any fields.
     */
    public NAppInitInfo() {
    }

    /**
     * Initializes an instance of {@code NAppInitInfo} with the specified arguments, application class,
     * store ID, and start time.
     *
     * @param args      The command-line arguments passed to the application.
     * @param appClass  The application's main class.
     * @param startTime The start time of the application, represented as an {@code NClock} instance.
     */
    public NAppInitInfo(String[] args, Class<?> appClass, NClock startTime) {
        this.args = args;
        this.appClass = appClass;
        this.startTime = startTime;
    }

    /**
     * Retrieves the command-line arguments passed to the application.
     *
     * @return An array of strings representing the command-line arguments*/
    public String[] getArgs() {
        return args;
    }

    /**
     * Sets the command-line arguments for the application.
     *
     * @param args The command-line arguments to be assigned.
     * @return The current instance of {@code NAppInitInfo} for method chaining.
     */
    public NAppInitInfo setArgs(String[] args) {
        this.args = args;
        return this;
    }

    /**
     * Retrieves the application's main class.
     *
     * @return The application's main class, represented as a {@code Class<?>} object.
     */
    public Class<?> getAppClass() {
        return appClass;
    }

    /**
     * Sets the application's main class.
     *
     * @param appClass The application's main class, represented as a {@code Class<?>} object.
     * @return The current instance of {@code NAppInitInfo} for method chaining.
     */
    public NAppInitInfo setAppClass(Class<?> appClass) {
        this.appClass = appClass;
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
