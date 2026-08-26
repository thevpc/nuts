package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.elem.NElementUtils;

/**
 * NStringLiteralFormat interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NStringLiteralFormat {
    NStringLiteralFormat JAVA_STRING = NStringLiteralFormatBuilder.ofJava(NElementType.DOUBLE_QUOTED_STRING).build();
    NStringLiteralFormat JAVA_NAME = text -> NElementUtils.isElementName(text) ? text : JAVA_STRING.format(text);
    NStringLiteralFormat JAVA_STRING_UNBOUND = NStringLiteralFormatBuilder.ofJava(NElementType.DOUBLE_QUOTED_STRING)
            .skipBoundaries(true).build();
    NStringLiteralFormat JAVA_CHAR = NStringLiteralFormatBuilder.ofJava(NElementType.SINGLE_QUOTED_STRING).build();
    NStringLiteralFormat SH_DOUBLE = NStringLiteralFormatBuilder.ofShell(NElementType.DOUBLE_QUOTED_STRING).build();
    NStringLiteralFormat SH_SINGLE = NStringLiteralFormatBuilder.ofShell(NElementType.SINGLE_QUOTED_STRING).build();
    NStringLiteralFormat SH_BACK = NStringLiteralFormatBuilder.ofShell(NElementType.BACKTICK_STRING).build();

    enum Mode implements NEnum{
        /**
         * Boundary char appears in the body encoded by {@code boundaryEscape}.
         * Surrounding quotes are added when {@code condition} requires it.
         */
        ESCAPE_CHAR,
        /**
         * Boundary char is encoded by {@code boundaryEscape} (typically
         * {@link NCharEscape#REPEAT}).  Usually used with SQL / Pascal / CSV.
         * Note: {@code REPEAT} mode and {@link NCharEscape#REPEAT} as the encoder
         * are independent — you can use any encoder with any mode.
         */
        REPEAT,
        /**
         * No closing boundary.  Every logical line in the input is emitted as
         * {@code linePrefix + encodedLine + lineSuffix}.  Use empty strings for
         * a plain pass-through that only applies {@code charEscapeSet}.
         */
        PREFIX;
        private final String id;

      /**
       * Mode.
       */
        Mode() {
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
        public static NOptional<Mode> parse(String value) {
            return NEnumUtils.parseEnum(value, Mode.class);
        }
    }

    /**
     * Format.
     *
     * @param text text
     * @return format result
     */
    String format(String text);
}
