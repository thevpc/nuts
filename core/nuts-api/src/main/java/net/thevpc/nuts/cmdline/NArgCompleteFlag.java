package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.io.NPropsTransformer;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * NArgCompleteFlag enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
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
  /**
   * N arg complete flag.
   */
    NArgCompleteFlag() {
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
    public static NOptional<NArgCompleteFlag> parse(String value) {
        return NEnumUtils.parseEnum(value, NArgCompleteFlag.class);
    }
}
