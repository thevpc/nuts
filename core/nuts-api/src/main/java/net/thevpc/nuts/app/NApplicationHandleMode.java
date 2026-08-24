package net.thevpc.nuts.app;

import net.thevpc.nuts.util.*;

/**
 * NApplicationHandleMode enum.
 *
 * @author thevpc
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
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    /**
                     * Runtime exception.
                     *
                     * @param e e
                     * @return runtime exception result
                     */
                    throw new RuntimeException(e);
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
