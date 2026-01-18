package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNErrorElementBuilder;
import net.thevpc.nuts.text.NMsg;

public class NDefaultErrorElement extends AbstractNElement implements NErrorElement {
    private NMsg message;

    public NDefaultErrorElement(NMsg message, NElementAnnotation[] annotations, NElementComments comments) {
        super(NElementType.ERROR, annotations, comments);
        this.message = message;
    }

    public NMsg getMessage() {
        return message;
    }

    @Override
    public String toString(boolean compact) {
        return "Error " + message;
    }

    @Override
    public NErrorElementBuilder builder() {
        return (NErrorElementBuilder)
                new DefaultNErrorElementBuilder()
                        .setMessage(message)
                        .addComments(comments())
                        .addAnnotations(annotations())
                ;
    }
}
