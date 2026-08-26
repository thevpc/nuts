package net.thevpc.nuts.io;

import net.thevpc.nuts.util.*;

/**
 * NpsStatus enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NpsStatus implements NEnum {
    IDLE,
    RUNNING,
    STOPPED,
    TERMINATED,
    DEAD,
    ZOMBIE,
    SUSPENDED,
    WAITING,
    TIMED_WAITING,
    WAITING_FOR_EVENT,
    WAITING_FOR_IO,
    BLOCKED,
    UNKNOWN
    ;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NpsStatus() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NpsStatus> parse(String value) {
        return NEnumUtils.parseEnum(value, NpsStatus.class);
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }


}
