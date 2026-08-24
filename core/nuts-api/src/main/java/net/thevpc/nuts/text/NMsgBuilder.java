package net.thevpc.nuts.text;

import net.thevpc.nuts.log.NMsgIntent;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * NMsgBuilder class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NMsgBuilder {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NMsgBuilder of() {
        return new NMsgBuilder();
    }

    /**
     * N msg builder.
     *
     * @return n msg builder result
     */
    public NMsgBuilder() {
    }

    private Level level;
    private NMsgIntent intent;
    private NMsg msg;
    private long time;
    private Supplier<NMsg> msgSupplier;

    /**
     * With intent.
     *
     * @param verb verb
     * @return with intent result
     */
    public NMsgBuilder withIntent(NMsgIntent verb) {
        this.intent = verb;
        return this;
    }

    /**
     * With level.
     *
     * @param level level
     * @return with level result
     */
    public NMsgBuilder withLevel(Level level) {
        this.level = level;
        return this;
    }

    /**
     * With msg plain.
     *
     * @param plain plain
     * @return with msg plain result
     */
    public NMsgBuilder withMsgPlain(String plain) {
        /**
         * With msg.
         *
         * @param NMsg.ofPlain(plain) n msg.of plain(plain)
         * @return with msg result
         */
        return withMsg(NMsg.ofPlain(plain));
    }

    /**
     * With msg ntf.
     *
     * @param plain plain
     * @return with msg ntf result
     */
    public NMsgBuilder withMsgNtf(String plain) {
        /**
         * With msg.
         *
         * @param NMsg.ofNtf(plain) n msg.of ntf(plain)
         * @return with msg result
         */
        return withMsg(NMsg.ofNtf(plain));
    }

    /**
     * With msg c.
     *
     * @param plain plain
     * @return with msg c result
     */
    public NMsgBuilder withMsgC(String plain) {
        /**
         * With msg.
         *
         * @param NMsg.ofC(plain) n msg.of c(plain)
         * @return with msg result
         */
        return withMsg(NMsg.ofC(plain));
    }

    /**
     * With msg c.
     *
     * @param plain plain
     * @param params params
     * @return with msg c result
     */
    public NMsgBuilder withMsgC(String plain, Object... params) {
        /**
         * With msg.
         *
         * @param params) params)
         * @return with msg result
         */
        return withMsg(NMsg.ofC(plain, params));
    }

    /**
     * With msg v.
     *
     * @param message message
     * @param params params
     * @return with msg v result
     */
    public NMsgBuilder withMsgV(String message, NMsgParam... params) {
        /**
         * With msg.
         *
         * @param params) params)
         * @return with msg result
         */
        return withMsg(NMsg.ofV(message, params));
    }

    /**
     * With msg v.
     *
     * @param message message
     * @param vars vars
     * @return with msg v result
     */
    public NMsgBuilder withMsgV(String message, Map<String, ?> vars) {
        /**
         * With msg.
         *
         * @param vars) vars)
         * @return with msg result
         */
        return withMsg(NMsg.ofV(message, vars));
    }

    /**
     * With msg v.
     *
     * @param message message
     * @param vars vars
     * @return with msg v result
     */
    public NMsgBuilder withMsgV(String message, Function<String, ?> vars) {
        /**
         * With msg.
         *
         * @param vars) vars)
         * @return with msg result
         */
        return withMsg(NMsg.ofV(message, vars));
    }


    /**
     * With msg m.
     *
     * @param message message
     * @param params params
     * @return with msg m result
     */
    public NMsgBuilder withMsgM(String message, NMsgParam... params) {
        /**
         * With msg.
         *
         * @param params) params)
         * @return with msg result
         */
        return withMsg(NMsg.ofM(message, params));
    }

    /**
     * With msg m.
     *
     * @param message message
     * @param vars vars
     * @return with msg m result
     */
    public NMsgBuilder withMsgM(String message, Map<String, ?> vars) {
        /**
         * With msg.
         *
         * @param vars) vars)
         * @return with msg result
         */
        return withMsg(NMsg.ofM(message, vars));
    }

    /**
     * With msg m.
     *
     * @param message message
     * @param vars vars
     * @return with msg m result
     */
    public NMsgBuilder withMsgM(String message, Function<String, ?> vars) {
        /**
         * With msg.
         *
         * @param vars) vars)
         * @return with msg result
         */
        return withMsg(NMsg.ofV(message, vars));
    }

    /**
     * With msg s.
     *
     * @param message message
     * @param params params
     * @return with msg s result
     */
    public NMsgBuilder withMsgS(String message, Object... params) {
        /**
         * With msg.
         *
         * @param params) params)
         * @return with msg result
         */
        return withMsg(NMsg.ofS(message, params));
    }

    /**
     * With msg s.
     *
     * @param message message
     * @param params params
     * @return with msg s result
     */
    public NMsgBuilder withMsgS(String message, NMsgParam... params) {
        /**
         * With msg.
         *
         * @param params) params)
         * @return with msg result
         */
        return withMsg(NMsg.ofS(message, params));
    }

    /**
     * With msg s.
     *
     * @param message message
     * @param vars vars
     * @return with msg s result
     */
    public NMsgBuilder withMsgS(String message, Map<String, ?> vars) {
        /**
         * With msg.
         *
         * @param vars) vars)
         * @return with msg result
         */
        return withMsg(NMsg.ofS(message, vars));
    }

    /**
     * With msg s.
     *
     * @param message message
     * @param vars vars
     * @return with msg s result
     */
    public NMsgBuilder withMsgS(String message, Function<String, ?> vars) {
        /**
         * With msg.
         *
         * @param vars) vars)
         * @return with msg result
         */
        return withMsg(NMsg.ofS(message, vars));
    }

    /**
     * With msg j.
     *
     * @param message message
     * @param params params
     * @return with msg j result
     */
    public NMsgBuilder withMsgJ(String message, NMsgParam... params) {
        /**
         * With msg.
         *
         * @param params) params)
         * @return with msg result
         */
        return withMsg(NMsg.ofJ(message, params));
    }

    /**
     * With msg j.
     *
     * @param message message
     * @param params params
     * @return with msg j result
     */
    public NMsgBuilder withMsgJ(String message, Object... params) {
        /**
         * With msg.
         *
         * @param params) params)
         * @return with msg result
         */
        return withMsg(NMsg.ofJ(message, params));
    }


    /**
     * With msg.
     *
     * @param message message
     * @return with msg result
     */
    public NMsgBuilder withMsg(NMsg message) {
        this.msg = message;
        if (msg != null) {
            this.msgSupplier = null;
        }
        return this;
    }

    /**
     * With msg.
     *
     * @param msgSupplier msg supplier
     * @return with msg result
     */
    public NMsgBuilder withMsg(Supplier<NMsg> msgSupplier) {
        this.msgSupplier = msgSupplier;
        if (msgSupplier != null) {
            this.msg = null;
        }
        return this;
    }

    /**
     * With time millis.
     *
     * @param time time
     * @return with time millis result
     */
    public NMsgBuilder withTimeMillis(long time) {
        this.time = time < 0 ? -1 : time * 1000000L;
        return this;
    }

    /**
     * With time nanos.
     *
     * @param time time
     * @return with time nanos result
     */
    public NMsgBuilder withTimeNanos(long time) {
        this.time = time < 0 ? -1 : time;
        return this;
    }

    /**
     * Build.
     *
     * @return build result
     */
    public NMsg build() {
        Level level = this.level;
        NMsg msgOk = null;
        if (level == null) {
            NMsg m = msg;
            if (msgSupplier != null) {
                m = msgSupplier.get();
            }
            if (m == null) {
                level = Level.FINEST;
            } else {
                msgOk = m;
                level = msgOk.level();
            }
        }
        if (msgOk == null) {
            NMsg m = msg;
            if (msgSupplier != null) {
                m = msgSupplier.get();
            }
            msgOk = m;
        }
        NMsg newMsg = msgOk;
        if (newMsg == null) {
            newMsg = NMsg.ofPlain("");
        }
        if (this.intent != null) {
            newMsg = newMsg.withIntent(intent);
        }
        newMsg = newMsg.withLevel(level);
        if (this.intent == null && newMsg.intent() == null) {
            switch (this.level.intValue()) {
                case 1000: {
                    newMsg = newMsg.withIntent(NMsgIntent.FAIL);
                    break;
                }
                case 900: {
                    newMsg = newMsg.withIntent(NMsgIntent.ALERT);
                    break;
                }
                case 800: {
                    newMsg = newMsg.withIntent(NMsgIntent.NOTICE);
                    break;
                }
                case 700: {
                    newMsg = newMsg.withIntent(NMsgIntent.INIT);
                    break;
                }
                case 500:
                case 400:
                case 300: {
                    newMsg = newMsg.withIntent(NMsgIntent.DEBUG);
                    break;
                }
            }
        }
        if (time >= 0) {
            newMsg = newMsg.withDurationNanos(time);
        }
        return newMsg;
    }
}
