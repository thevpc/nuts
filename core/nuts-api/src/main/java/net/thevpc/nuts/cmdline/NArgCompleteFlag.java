package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.io.NPropsTransformer;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NArgCompleteFlag implements NEnum {
    NOSPACE,
    PLUSDIRS,
    NOSORT,
    NOQUOTE,
    FILENAMES,
    DIRNAMES,
    NOFILE,
    ERROR;
    private final String id;
    NArgCompleteFlag() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NOptional<NArgCompleteFlag> parse(String value) {
        return NEnumUtils.parseEnum(value, NArgCompleteFlag.class);
    }
}
