package net.thevpc.nuts.app;

import net.thevpc.nuts.util.*;

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
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void runHandled(Runnable preparedWorkspace, NApplicationHandleMode handleMode) {
        NApplicationHandleMode m = NUtils.firstNonNull(handleMode, NApplicationHandleMode.HANDLE);
        m.runHandled(preparedWorkspace);
    }
}
