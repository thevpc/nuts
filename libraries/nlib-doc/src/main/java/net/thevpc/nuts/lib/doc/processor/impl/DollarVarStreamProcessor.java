package net.thevpc.nuts.lib.doc.processor.impl;

import net.thevpc.nuts.lib.doc.mimetype.MimeTypeConstants;
import net.thevpc.nuts.lib.doc.processor.base.TagStreamProcessor;

/**
 * something
 * ${:if }
 * ${:else if }
 * ${:else}
 * ${:end}
 * ${:for}
 */
public class DollarVarStreamProcessor extends TagStreamProcessor {
    public static final DollarVarStreamProcessor INSTANCE = new DollarVarStreamProcessor();
    public DollarVarStreamProcessor() {
        super("${","}",MimeTypeConstants.NEXPR);
    }
}
