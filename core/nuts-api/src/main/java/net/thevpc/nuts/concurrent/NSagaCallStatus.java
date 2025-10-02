package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * @since 0.8.7
 */
public enum NSagaCallStatus implements NEnum {
    PENDING,        // saga has not yet begun
    RUNNING,            // saga is currently executing
    SUCCESS,   // saga finished normally without errors
    ROLLED_BACK,       // some steps failed, but all compensations succeeded
    PARTIAL_ROLLBACK,       // some steps failed, some compensations ignored/skipped (strategy=IGNORE)
    FAILED; // some step could not be compensated (strategy=ABORT)

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NSagaCallStatus() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NSagaCallStatus> parse(String value) {
        return NEnumUtils.parseEnum(value, NSagaCallStatus.class);
    }

    @Override
    public String id() {
        return id;
    }
}
