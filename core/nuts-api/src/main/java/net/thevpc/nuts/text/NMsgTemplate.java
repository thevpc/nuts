package net.thevpc.nuts.text;

import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class NMsgTemplate {
    private final String message;
    private final String customMessageId;
    private final NMsgType format;
    private static Set<NMsgType> ACCEPTED_FORMATS = new HashSet<>(Arrays.asList(
            NMsgType.CFORMAT,
            NMsgType.JFORMAT,
            NMsgType.VFORMAT,
            NMsgType.MFORMAT,
            NMsgType.SFORMAT,
            NMsgType.CUSTOM
    ));

    public static NMsgTemplate ofC(String message) {
        return of(message, NMsgType.CFORMAT,null);
    }

    public static NMsgTemplate ofJ(String message) {
        return of(message, NMsgType.JFORMAT,null);
    }

    public static NMsgTemplate ofS(String message) {
        return of(message, NMsgType.SFORMAT,null);
    }

    public static NMsgTemplate ofV(String message) {
        return of(message, NMsgType.VFORMAT,null);
    }

    public static NMsgTemplate ofM(String message) {
        return of(message, NMsgType.MFORMAT,null);
    }
    public static NMsgTemplate ofCustom(String messageTypeId,String message) {
        return of(message, NMsgType.CUSTOM,messageTypeId);
    }

    public static NMsgTemplate of(String message, NMsgType format, String customMessageIt) {
        return new NMsgTemplate(message, format,customMessageIt);
    }

    public NMsgTemplate(String message, NMsgType format, String customMessageId) {
        NAssert.requireNamedNonNull(message, "message");
        NAssert.requireNamedNonNull(format, "format");
        switch (format) {
            case CFORMAT:
            case JFORMAT:
            case VFORMAT:
            case MFORMAT:
            case SFORMAT: {
                this.customMessageId = null;
                break;
            }
            case CUSTOM: {
                NAssert.requireNamedNonBlank(customMessageId, "customMessageIt");
                this.customMessageId = NStringUtils.strip(customMessageId);
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid format. only "+ACCEPTED_FORMATS+" are allowed");
            }
        }
        this.message = message;
        this.format = format;
    }

    public String customMessageId() {
        return customMessageId;
    }

    public String message() {
        return message;
    }

    public List<String> paramNames() {
        try {
            Set<String> paramSet = new HashSet<>();
            List<String> params = new ArrayList<>();
            switch (format) {
                case CFORMAT: {
                    StringReader r = new StringReader(message);
                    while (true) {
                        int i = r.read();
                        if (i < 0) {
                            break;
                        }
                        char c = (char) i;
                        if (c == '%') {
                            i = r.read();
                            if (i >= 0) {
                                char c2 = (char) i;
                                if (c2 == 'n') {
                                    //ignore
                                } else {
                                    params.add("");
                                }
                            }
                        }
                    }
                    break;
                }
                case JFORMAT: {
                    StringReader r = new StringReader(message);
                    int maxElems = 0;
                    int currentIndex = -1;
                    while (true) {
                        int i = r.read();
                        if (i < 0) {
                            break;
                        }
                        char c = (char) i;
                        if (c == '{') {
                            StringBuilder n = new StringBuilder();
                            while (true) {
                                i = r.read();
                                if (i < 0) {
                                    break;
                                }
                                c = (char) i;
                                if (c == '\\') {
                                    i = r.read();
                                    if (i < 0) {
                                        break;
                                    } else {
                                        n.append(c = (char) i);
                                    }
                                } else if (c == '}') {
                                    break;
                                } else {
                                    n.append(c);
                                }
                            }
                            currentIndex++;
                            String ns = n.toString();
                            int sep = ns.indexOf(':');
                            String nsIntString = "";
                            Integer nsInt = null;
                            if (sep < 0) {
                                nsIntString = NStringUtils.strip(ns);
                            } else {
                                nsIntString = NStringUtils.strip(ns.substring(0, sep));
                            }
                            if (nsIntString.isEmpty()) {
                                nsIntString = String.valueOf(currentIndex);
                            }
                            NLiteral lit = NLiteral.of(nsIntString);
                            if (lit.asInt().isPresent()) {
                                nsInt = lit.asInt().get();
                            }
                            if (nsInt != null) {
                                while (maxElems <= nsInt) {
                                    if (paramSet.add(String.valueOf(maxElems))) {
                                        params.add(String.valueOf(maxElems));
                                    }
                                    maxElems++;
                                }
                            } else {
                                if (paramSet.add(nsIntString)) {
                                    params.add(nsIntString);
                                }
                            }
                        } else if (c == '\\') {
                            r.read();
                        } else {
                            //ignore
                        }
                    }
                    break;
                }
                case VFORMAT: {
                    NStringUtils.parseDollarPlaceHolder(message).forEach(s -> {
                        if (s.ttype == NToken.TT_DOLLAR || s.ttype == NToken.TT_DOLLAR_BRACE) {
                            String ns = s.sval;
                            if (paramSet.add(ns)) {
                                params.add(ns);
                            }
                        }
                    });
                    break;
                }
                case MFORMAT: {
                    return NUtilsRPI.of().extractMessageParams(message, NMsgType.MFORMAT, null);
                }
                case SFORMAT: {
                    return NUtilsRPI.of().extractMessageParams(message, NMsgType.SFORMAT, null);
                }
                case CUSTOM:{
                    return NUtilsRPI.of().extractMessageParams(message, NMsgType.CUSTOM, customMessageId());
                }
                default: {
                    throw new IllegalArgumentException("invalid format. only "+ACCEPTED_FORMATS+" are allowed");
                }
            }
            return params;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NMsgType format() {
        return format;
    }

    public NMsg build(NMsgParam... params) {
        Set<String> required = new HashSet<>(paramNames());
        Set<String> provided = new HashSet<>();
        if (params != null) {
            for (NMsgParam p : params) {
                provided.add(p.name());
            }
        }
        if (!provided.containsAll(required)) {
            Set<String> missing = new HashSet<>(required);
            missing.removeAll(provided);
            throw new NIllegalArgumentException(NMsg.ofC("missing template parameters %s", missing));
        }
        switch (format) {
            case CFORMAT: {
                return NMsg.ofC(message, params);
            }
            case VFORMAT: {
                return NMsg.ofV(message, params);
            }
            case MFORMAT: {
                return NMsg.ofM(message, params);
            }
            case JFORMAT: {
                return NMsg.ofJ(message, params);
            }
            case SFORMAT: {
                return NMsg.ofS(message, params);
            }
            case CUSTOM: {
                return NMsg.ofCustom(customMessageId,message, params);
            }
            default: {
                throw new IllegalArgumentException("invalid format. only "+ACCEPTED_FORMATS+" are allowed");
            }
        }
    }
}
