package net.thevpc.nuts.lib.doc.processor.impl;

import net.thevpc.nuts.lib.doc.mimetype.MimeTypeConstants;
import net.thevpc.nuts.lib.doc.processor.base.TagStreamProcessor;

public class DollarBracket2VarStreamProcessor extends TagStreamProcessor {
    public static final DollarBracket2VarStreamProcessor INSTANCE = new DollarBracket2VarStreamProcessor();

    public DollarBracket2VarStreamProcessor() {
        super("${{","}}",MimeTypeConstants.NEXPR);
    }

}
