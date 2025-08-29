package net.thevpc.nuts.util;

import net.thevpc.nuts.log.NMsgIntent;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public class NMsgBuilder {

    public static NMsgBuilder of() {
        return new NMsgBuilder();
    }

    public NMsgBuilder() {
    }

    private Level level;
    private NMsgIntent intent;
    private NMsg msg;
    private long time;
    private Supplier<NMsg> msgSupplier;

    public NMsgBuilder withIntent(NMsgIntent verb) {
        this.intent = verb;
        return this;
    }

    public NMsgBuilder withLevel(Level level) {
        this.level = level;
        return this;
    }

    public NMsgBuilder withMsgPlain(String plain) {
        return withMsg(NMsg.ofPlain(plain));
    }

    public NMsgBuilder withMsgNtf(String plain) {
        return withMsg(NMsg.ofNtf(plain));
    }
    public NMsgBuilder withMsgC(String plain) {
        return withMsg(NMsg.ofC(plain));
    }

    public NMsgBuilder withMsgC(String plain, Object... params) {
        return withMsg(NMsg.ofC(plain, params));
    }

    public NMsgBuilder withMsgV(String message, NMsgParam... params) {
        return withMsg(NMsg.ofV(message, params));
    }

    public NMsgBuilder withMsgV(String message, Map<String, ?> vars) {
        return withMsg(NMsg.ofV(message, vars));
    }

    public NMsgBuilder withMsgV(String message, Function<String, ?> vars) {
        return withMsg(NMsg.ofV(message, vars));
    }

    public NMsgBuilder withMsgJ(String message, NMsgParam... params) {
        return withMsg(NMsg.ofJ(message, params));
    }

    public NMsgBuilder withMsgJ(String message, Object... params) {
        return withMsg(NMsg.ofJ(message, params));
    }

    public NMsgBuilder withMsg(NMsg message) {
        this.msg = message;
        if (msg != null) {
            this.msgSupplier = null;
        }
        return this;
    }

    public NMsgBuilder withMsg(Supplier<NMsg> msgSupplier) {
        this.msgSupplier = msgSupplier;
        if (msgSupplier != null) {
            this.msg = null;
        }
        return this;
    }

    public NMsgBuilder withTimeMillis(long time) {
        this.time = time < 0 ? -1 : time * 1000000L;
        return this;
    }

    public NMsgBuilder withTimeNanos(long time) {
        this.time = time < 0 ? -1 : time;
        return this;
    }

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
                level = msgOk.getLevel();
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
        if (this.intent == null && newMsg.getIntent() == null) {
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
                    newMsg = newMsg.withIntent(NMsgIntent.INFO);
                    break;
                }
                case 700: {
                    newMsg = newMsg.withIntent(NMsgIntent.CONFIG);
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
