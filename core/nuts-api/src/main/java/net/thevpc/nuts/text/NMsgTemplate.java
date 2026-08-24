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

/**
 * NMsgTemplate class.
 *
 * @author thevpc
 * @since 0.8.0
 */
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

    /**
     * Creates a new instance of of c.
     *
     * @param message message
     * @return of c result
     */
    public static NMsgTemplate ofC(String message) {
        /**
         * Creates a new instance of of.
         *
         * @param message message
         * @param NMsgType.CFORMAT n msg type.cformat
         * @param null null
         * @return of result
         */
        return of(message, NMsgType.CFORMAT,null);
    }

    /**
     * Creates a new instance of of j.
     *
     * @param message message
     * @return of j result
     */
    public static NMsgTemplate ofJ(String message) {
        /**
         * Creates a new instance of of.
         *
         * @param message message
         * @param NMsgType.JFORMAT n msg type.jformat
         * @param null null
         * @return of result
         */
        return of(message, NMsgType.JFORMAT,null);
    }

    /**
     * Creates a new instance of of s.
     *
     * @param message message
     * @return of s result
     */
    public static NMsgTemplate ofS(String message) {
        /**
         * Creates a new instance of of.
         *
         * @param message message
         * @param NMsgType.SFORMAT n msg type.sformat
         * @param null null
         * @return of result
         */
        return of(message, NMsgType.SFORMAT,null);
    }

    /**
     * Creates a new instance of of v.
     *
     * @param message message
     * @return of v result
     */
    public static NMsgTemplate ofV(String message) {
        /**
         * Creates a new instance of of.
         *
         * @param message message
         * @param NMsgType.VFORMAT n msg type.vformat
         * @param null null
         * @return of result
         */
        return of(message, NMsgType.VFORMAT,null);
    }

    /**
     * Creates a new instance of of m.
     *
     * @param message message
     * @return of m result
     */
    public static NMsgTemplate ofM(String message) {
        /**
         * Creates a new instance of of.
         *
         * @param message message
         * @param NMsgType.MFORMAT n msg type.mformat
         * @param null null
         * @return of result
         */
        return of(message, NMsgType.MFORMAT,null);
    }
    /**
     * Creates a new instance of of custom.
     *
     * @param messageTypeId message type id
     * @param message message
     * @return of custom result
     */
    public static NMsgTemplate ofCustom(String messageTypeId,String message) {
        /**
         * Creates a new instance of of.
         *
         * @param message message
         * @param NMsgType.CUSTOM n msg type.custom
         * @param messageTypeId message type id
         * @return of result
         */
        return of(message, NMsgType.CUSTOM,messageTypeId);
    }

    /**
     * Creates a new instance of of.
     *
     * @param message message
     * @param format format
     * @param customMessageIt custom message it
     * @return of result
     */
    public static NMsgTemplate of(String message, NMsgType format, String customMessageIt) {
        return new NMsgTemplate(message, format,customMessageIt);
    }

    /**
     * N msg template.
     *
     * @param message message
     * @param format format
     * @param customMessageId custom message id
     * @return n msg template result
     */
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
                /**
                 * Illegal argument exception.
                 *
                 * @param allowed" allowed"
                 * @return illegal argument exception result
                 */
                throw new IllegalArgumentException("invalid format. only "+ACCEPTED_FORMATS+" are allowed");
            }
        }
        this.message = message;
        this.format = format;
    }

    /**
     * Custom message id.
     *
     * @return custom message id result
     */
    public String customMessageId() {
        return customMessageId;
    }

    /**
     * Message.
     *
     * @return message result
     */
    public String message() {
        return message;
    }

    /**
     * Param names.
     *
     * @return param names result
     */
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
                    /**
                     * Illegal argument exception.
                     *
                     * @param allowed" allowed"
                     * @return illegal argument exception result
                     */
                    throw new IllegalArgumentException("invalid format. only "+ACCEPTED_FORMATS+" are allowed");
                }
            }
            return params;
        } catch (IOException e) {
            /**
             * Runtime exception.
             *
             * @param e e
             * @return runtime exception result
             */
            throw new RuntimeException(e);
        }
    }

    /**
     * Format.
     *
     * @return format result
     */
    public NMsgType format() {
        return format;
    }

    /**
     * Build.
     *
     * @param params params
     * @return build result
     */
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
            /**
             * N illegal argument exception.
             *
             * @param missing) missing)
             * @return n illegal argument exception result
             */
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
                /**
                 * Illegal argument exception.
                 *
                 * @param allowed" allowed"
                 * @return illegal argument exception result
                 */
                throw new IllegalArgumentException("invalid format. only "+ACCEPTED_FORMATS+" are allowed");
            }
        }
    }
}
