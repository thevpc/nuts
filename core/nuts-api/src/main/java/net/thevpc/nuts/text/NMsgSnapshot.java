package net.thevpc.nuts.text;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.*;

import java.util.logging.Level;

/**
 *
 * @since 1.1.0
 */
public class NMsgSnapshot implements NToElement, NCopiable, Cloneable {
    private final NText text;
    private final Level level;
    private final NMsgIntent intent;
    private final NDuration duration;

    public static NMsgSnapshot of(NMsg msg) {
        return msg == null ? null : new NMsgSnapshot(msg);
    }

    public NMsgSnapshot(NMsg msg) {
        NAssert.requireNamedNonNull(msg, "msg");
        this.text = NText.of(msg);
        this.level = msg.level();
        this.intent = msg.intent();
        this.duration = msg.duration();
    }

    public NMsgSnapshot(NText text, Level level, NMsgIntent intent, NDuration duration) {
        this.text = text;
        this.level = level;
        this.intent = intent;
        this.duration = duration;
    }

    public static NOptional<NMsgSnapshot> of(NElement elem) {
        if (elem == null || elem.isNull()) {
            return NOptional.ofNamedEmpty("normalized msg");
        }
        if (elem instanceof NStringElement || elem instanceof NPrimitiveElement) {
            String s = elem.asStringValue().orNull();
            return NOptional.of(new NMsgSnapshot(NMsg.ofP(s == null ? "" : s)));
        }
        if (!(elem instanceof NObjectElement)) {
            return NOptional.ofNamedEmpty("normalized msg");
        }
        NObjectElement o = (NObjectElement) elem;

        String stext = o.get("text").map(x -> x.asStringValue().orNull()).orElse("");
        Level level = o.get("level").map(x -> {
            String sl = x.asStringValue().orNull();
            if (NBlankable.isBlank(sl)) return null;
            try {
                return Level.parse(sl);
            } catch (Exception ex) {
                return null;
            }
        }).orNull();

        NMsgIntent intent = o.get("intent").map(x -> {
            String sl = x.asStringValue().orNull();
            if (NBlankable.isBlank(sl)) return null;
            try {
                return NMsgIntent.of(sl);
            } catch (Exception ex) {
                return null;
            }
        }).orNull();

        NDuration dur = o.get("duration").map(x -> {
            String sl = x.asStringValue().orNull();
            if (NBlankable.isBlank(sl)) return null;
            try {
                return NDuration.of(sl).orNull();
            } catch (Exception ex) {
                return null;
            }
        }).orNull();

        return NOptional.of(new NMsgSnapshot(
                NMsg.ofNtf(stext)
                        .withDuration(dur)
                        .withIntent(intent)
                        .withLevel(level)
        ));
    }

    public NMsg toMsg() {
        boolean plain = isPlainFormat();
        if (plain) {
            return NMsg.ofP(text == null ? "" : text.toString());
        }
        return NMsg.ofNtf(text)
                .withDuration(duration)
                .withIntent(intent)
                .withLevel(level);
    }

    @Override
    public NElement toElement() {
        if (isPlainFormat()) {
            return NElement.ofString(text == null ? "" : text.toString());
        }
        NObjectElementBuilder b = NObjectElementBuilder.of();
        b.add("text", text == null ? "" : text.toString());
        if (level != null) {
            b.add("level", level.getName());
        }
        if (intent != null) {
            b.add("intent", intent.name());
        }
        if (duration != null) {
            b.add("duration", duration.toString());
        }
        return b.build();
    }

    private boolean isPlainFormat() {
        return intent == null
                && duration == null
                && (level == null || level.intValue() == Level.INFO.intValue())
                && (text == null || text instanceof NTextPlain);
    }

    @Override
    public NMsgSnapshot clone() {
        try {
            return (NMsgSnapshot) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new NUnexpectedException(NMsg.ofC("clone unsupported"), e);
        }
    }

    @Override
    public NMsgSnapshot copy() {
        return clone();
    }
}
