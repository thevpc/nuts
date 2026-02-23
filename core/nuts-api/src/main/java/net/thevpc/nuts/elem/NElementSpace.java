package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

public interface NElementSpace extends NAffix{
    static NElementSpace of() {
        return NElementFactory.of().ofSpace(" ");
    }

    static NElementSpace of(int count) {
        NAssert.requireTrue(count > 0, () -> NMsg.ofC("spaces count should be positive"));
        return NElementFactory.of().ofSpace(NStringUtils.repeat(' ', count));
    }

    static NElementSpace of(String value) {
        return NElementFactory.of().ofSpace(value);
    }

    String value();
}
