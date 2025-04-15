package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser.javacc;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonParseException;

public class JavaccHelper {
    public static TsonParseException createTsonParseException(ParseException ex, Object source) {
        return new TsonParseException(
                ex.getMessage()
                , ex.currentToken.toString()
                , ex.expectedTokenSequences
                , ex.tokenImage
                , ex.currentToken.kind
                , ex.currentToken.beginLine
                , ex.currentToken.beginColumn
                , ex.currentToken.endLine
                , ex.currentToken.endColumn
                , ex.currentToken.image,
                source
        );
    }

}
