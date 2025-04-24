package net.thevpc.nuts.io;

import net.thevpc.nuts.util.*;

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
