package net.thevpc.nuts.app;

import net.thevpc.nuts.util.*;

/**
 * Controls how the application launcher handles an exception raised while preparing
 * or running an application.
 *
 * <p>{@link #HANDLE} delegates to Nuts' exception handler, {@link #PROPAGATE}
 * rethrows through it, {@link #EXIT} terminates the process after handling the
 * outcome, and {@link #NOP} converts a caught exception to an unchecked exception.</p>
 *
 * @since 0.8.0
 */
public enum NApplicationHandleMode implements NEnum {
    HANDLE,
    PROPAGATE,
    EXIT,
    NOP;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NApplicationHandleMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NApplicationHandleMode> parse(String value) {
        return NEnumUtils.parseEnum(value, NApplicationHandleMode.class);
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }


    /**
     * Run handled.
     *
     * @param preparedWorkspace prepared workspace
     */
    public void runHandled(Runnable preparedWorkspace) {
        try {
            preparedWorkspace.run();
            switch (this) {
                case EXIT: {
                    System.exit(0);
                    break;
                }
            }
        } catch (Exception e) {
            switch (this) {
                case PROPAGATE: {
                    NExceptionHandler.of(e).propagate();
                    break;
                }
                case EXIT: {
                    NExceptionHandler.of(e).handleFatal();
                    break;
                }
                case HANDLE: {
                    NExceptionHandler.of(e).handle();
                    break;
                }
                default: {
                    throw NException.ofUncheckedException(e);
                }
            }
        }
    }

    /**
     * Run handled.
     *
     * @param preparedWorkspace prepared workspace
     * @param handleMode handle mode
     */
    public static void runHandled(Runnable preparedWorkspace, NApplicationHandleMode handleMode) {
        NApplicationHandleMode m = NUtils.firstNonNull(handleMode, NApplicationHandleMode.HANDLE);
        m.runHandled(preparedWorkspace);
    }
}
