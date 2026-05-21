package net.thevpc.nuts.text;

import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NMsgTemplate {
    private final String message;
    private final NTextFormatType format;

    public static NMsgTemplate ofC(String message) {
        return of(message, NTextFormatType.CFORMAT);
    }

    public static NMsgTemplate ofJ(String message) {
        return of(message, NTextFormatType.JFORMAT);
    }

    public static NMsgTemplate ofV(String message) {
        return of(message, NTextFormatType.VFORMAT);
    }

    public static NMsgTemplate ofM(String message) {
        return of(message, NTextFormatType.MFORMAT);
    }

    public static NMsgTemplate of(String message, NTextFormatType format) {
        return new NMsgTemplate(message, format);
    }

    public NMsgTemplate(String message, NTextFormatType format) {
        NAssert.requireNamedNonNull(message, "message");
        NAssert.requireNamedNonNull(format, "format");
        switch (format) {
            case CFORMAT:
            case JFORMAT:
            case VFORMAT:
            case MFORMAT: {
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid format. only Cformat, JFormat, VFormat and MFormat are allowed");
            }
        }
        this.message = message;
        this.format = format;
    }

    public String message() {
        return message;
    }

    public String[] paramNames() {
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
                                nsIntString = ns.trim();
                            } else {
                                nsIntString = ns.substring(0, sep).trim();
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
                    NStringUtils.parseMoustachePlaceHolder(message).forEach(s -> {
                        if (s.ttype == NToken.TT_MOUSTACHE_START) {
                            String ns = s.sval;
                            if (paramSet.add(ns)) {
                                params.add(ns);
                            }
                        }
                    });
                    break;
                }
                default: {
                    throw new IllegalArgumentException("invalid format. only Cformat, JFormat, VFormat and MFormat are allowed");
                }
            }
            return params.toArray(new String[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NTextFormatType format() {
        return format;
    }

    public NMsg build(NMsgParam... params) {
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
            default: {
                throw new IllegalArgumentException("invalid format. only Cformat, JFormat, VFormat, and MFormat are allowed");
            }
        }
    }
}
