package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * @since 0.8.7
 */
public enum NSagaCallNodeStatus implements NEnum {
    PENDING,
    RUNNING,
    FINISHED,
    FAILED,
    IGNORED,        // step failed but strategy=IGNORE
    COMPENSATING,
    COMPENSATED,
    COMPENSATION_FAILED,
    COMPENSATION_IGNORED;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NSagaCallNodeStatus() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NSagaCallNodeStatus> parse(String value) {
        return NEnumUtils.parseEnum(value, NSagaCallNodeStatus.class);
    }

    @Override
    public String id() {
        return id;
    }
}
