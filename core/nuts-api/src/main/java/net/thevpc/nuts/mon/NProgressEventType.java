package net.thevpc.nuts.mon;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

/**
 * NProgressEventType enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NProgressEventType implements NEnum {
    START,
    SUSPEND,
    UNDO_SUSPEND,
    CANCEL,
    UNDO_CANCEL,
    COMPLETE,
    UNDO_COMPLETE,
    BLOCK,
    UNDO_BLOCK,
    PROGRESS,
    MESSAGE,
    RESET,
    UPDATE;
    private String id;

  /**
   * N progress event type.
   */
    NProgressEventType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NProgressEventType> parse(String value) {
        return NEnumUtils.parseEnum(value, NProgressEventType.class);
    }
}
