package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

public interface NCmdLineNamedAction {
    boolean isAcceptable();

    /**
     * consume next argument with boolean value and run {@code consumer}
     *
     * @return true if active
     */
    boolean consumeFlag(NArgProcessor<Boolean> consumer);

    boolean consumeOptionalFlag(NArgProcessor<NOptional<Boolean>> consumer);

    boolean consumeTrueFlag(NArgProcessor<Boolean> consumer);


    boolean consumeOptionalEntry(NArgProcessor<NOptional<String>> consumer);

    /**
     * consume next argument with string value and run {@code consumer}
     *
     * @return true if active
     */
    boolean consumeEntry(NArgProcessor<String> consumer);

    /**
     * consume next argument and run {@code consumer}
     *
     * @return true if active
     */
    boolean consumeEntryValue(NArgProcessor<NLiteral> consumer);

}
