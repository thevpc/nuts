package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsOptional;

public enum NutsProgressEventType implements NutsEnum {
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

    NutsProgressEventType() {
        this.id = NutsNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NutsOptional<NutsProgressEventType> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsProgressEventType.class);
    }
}
