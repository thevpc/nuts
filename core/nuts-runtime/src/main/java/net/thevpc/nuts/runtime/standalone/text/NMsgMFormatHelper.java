package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class NMsgMFormatHelper extends AbstractNMsgFormatHelper {

    private Function<String, NText> mapper = null;

    public NMsgMFormatHelper(NMsg m, NTextRPI txt) {
        super(m);
        Object param = params == null ? (Collections.emptyMap()) : params[0];
        if (param instanceof Map) {
            mapper = x -> {
                Object u = ((Map<String, ?>) param).get(x);
                if (u == null) {
                    return null;
                }
                return NText.of(u);
            };
        } else {
            Function<String, ?> f = (Function<String, ?>) param;
            mapper = x -> {
                Object u = f.apply(x);
                if (u == null) {
                    Object v = applyPlaceholder(x);
                    if (v != null) {
                        u = v;
                    }
                }
                u=resolvePlaceholder(u);
                return NText.of(u);
            };
        }
    }


    protected NText formatPlain(String ss) {
        if (ss == null) {
            return NText.ofBlank();
        }
        if (ss.indexOf('{') < 0) {
            return NText.ofPlain(ss);
        }
        NTextBuilder sb = NTextBuilder.of();
        NStringUtils.parseMoustachePlaceHolder(ss).forEach(t -> {
            switch (t.ttype) {
                case NToken.TT_MOUSTACHE_START: {
                    NText x = mapper.apply(t.sval);
                    if (x == null) {
                        throw new IllegalArgumentException("msg var not found " + t.sval);
                    }
                    sb.append(x);
                    break;
                }
                default:
                    sb.append(NText.ofPlain(t.sval));
            }
        });
        return sb.build();
    }
}
