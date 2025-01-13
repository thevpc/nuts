package net.thevpc.nuts.lib.doc.processor.impl;

import net.thevpc.nuts.lib.doc.mimetype.MimeTypeConstants;
import net.thevpc.nuts.lib.doc.processor.base.TagStreamProcessor;

public class Bracket2VarStreamProcessor extends TagStreamProcessor {
    public static final Bracket2VarStreamProcessor INSTANCE = new Bracket2VarStreamProcessor();

    public Bracket2VarStreamProcessor() {
        super("{{","}}",MimeTypeConstants.NEXPR);
    }

}
