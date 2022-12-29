package net.thevpc.nuts.util;

import net.thevpc.nuts.NOptional;

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

    NProgressEventType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NOptional<NProgressEventType> parse(String value) {
        return NStringUtils.parseEnum(value, NProgressEventType.class);
    }
}
