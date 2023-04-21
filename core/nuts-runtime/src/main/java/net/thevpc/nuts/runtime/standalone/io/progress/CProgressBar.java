package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogVerb;

/**
 * inspired by
 * https://github.com/Changaco/unicode-progress-bars/blob/master/generator.html
 *
 * @author thevpc
 */
public class CProgressBar {

    private static final IndeterminatePosition DEFAULT_INDETERMINATE_POSITION = new DefaultIndeterminatePosition();
    private int determinateSize = 10;
    private int indeterminateSize = 10;
    private int maxMessage = 0;
    private float indeterminateRatio = 0.3f;
    private NSession session;
    private NLog logger;
    private int columns = 3;
    private int maxColumns = 133;
    private boolean suffixMoveLineStart = true;
    private boolean prefixMoveLineStart = true;
    private long lastPrint = 0;
    private long minPeriod = 0;
    private IndeterminatePosition indeterminatePosition = DEFAULT_INDETERMINATE_POSITION;
    private ProgressOptions options;
    private Formatter formatter;
    private NWorkspace ws;
    private static Map<String, Function<NSession, Formatter>> formatters = new HashMap();

    static {
        reg("", session -> {
            NTexts txt = NTexts.of(session);
            return CorePlatformUtils.SUPPORTS_UTF_ENCODING ? createFormatter("braille", session) : createFormatter("simple", session);
        });
        reg("square", session -> {
            NTexts txt = NTexts.of(session);
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            return new SimpleFormatter("square",
                    new NText[]{
                            txt.ofStyled("⬜", NTextStyle.primary1()),
                            txt.ofStyled("⬛", NTextStyle.primary1()),
                    },
                    new NText[]{txt.ofStyled("⬛", NTextStyle.primary1())},
                    10,
                    -1, 10, 10
            );
        });
        reg("vbar", session -> {
            //" ▁▂▃▄▅▆▇█"
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            NTexts txt = NTexts.of(session);
            return new SimpleFormatter("vbar",
                    new NText[]{
                            txt.ofStyled(" ", NTextStyle.primary1()),
                            txt.ofStyled("▁", NTextStyle.primary1()),
                            txt.ofStyled("▂", NTextStyle.primary1()),
                            txt.ofStyled("▃", NTextStyle.primary1()),
                            txt.ofStyled("▄", NTextStyle.primary1()),
                            txt.ofStyled("▅", NTextStyle.primary1()),
                            txt.ofStyled("▆", NTextStyle.primary1()),
                            txt.ofStyled("▇", NTextStyle.primary1()),
                            txt.ofStyled("█", NTextStyle.primary1()),
                    },
                    new NText[]{
                            txt.ofStyled("▁", NTextStyle.primary1()),
                            txt.ofStyled("▂", NTextStyle.primary1()),
                            txt.ofStyled("▃", NTextStyle.primary1()),
                            txt.ofStyled("▄", NTextStyle.primary1()),
                            txt.ofStyled("▅", NTextStyle.primary1()),
                            txt.ofStyled("▆", NTextStyle.primary1()),
                            txt.ofStyled("▇", NTextStyle.primary1()),
                            txt.ofStyled("█", NTextStyle.primary1()),
                    },
                    new NText[]{
                            txt.ofStyled("█", NTextStyle.primary1()),
                            txt.ofStyled("▇", NTextStyle.primary1()),
                            txt.ofStyled("▆", NTextStyle.primary1()),
                            txt.ofStyled("▅", NTextStyle.primary1()),
                            txt.ofStyled("▄", NTextStyle.primary1()),
                            txt.ofStyled("▃", NTextStyle.primary1()),
                            txt.ofStyled("▂", NTextStyle.primary1()),
                            txt.ofStyled("▁", NTextStyle.primary1()),
                    },
                    1, 1, 10, 10
            );
        });

        reg("shadow", session -> {
            NTexts txt = NTexts.of(session);
            //" ░▒▓█"
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            return new SimpleFormatter("shadow",
                    new NText[]{
                            txt.ofStyled(" ", NTextStyle.primary1()),
                            txt.ofStyled("░", NTextStyle.primary1()),
                            txt.ofStyled("▒", NTextStyle.primary1()),
                            txt.ofStyled("▓", NTextStyle.primary1()),
                            txt.ofStyled("█", NTextStyle.primary1()),
                    },
                    new NText[]{
                            txt.ofStyled("█", NTextStyle.primary1()),
                    },
                    1, 1, 10, 10
            );
        });
        reg("hbar", session -> {
            NTexts txt = NTexts.of(session);
            //" ▏▎▍▌▋▊▉█"
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            return new SimpleFormatter("hbar",
                    new NText[]{
                            txt.ofStyled(" ", NTextStyle.primary1()),
                            txt.ofStyled("▏", NTextStyle.primary1()),
                            txt.ofStyled("▎", NTextStyle.primary1()),
                            txt.ofStyled("▍", NTextStyle.primary1()),
                            txt.ofStyled("▌", NTextStyle.primary1()),
                            txt.ofStyled("▋", NTextStyle.primary1()),
                            txt.ofStyled("▊", NTextStyle.primary1()),
                            txt.ofStyled("▉", NTextStyle.primary1()),
                            txt.ofStyled("█", NTextStyle.primary1()),
                    },
                    new NText[]{
                            txt.ofStyled("█", NTextStyle.primary1()),
                    },
                    10, -1, 10, 10
            );
        });
        reg("circle", session -> {
            NTexts txt = NTexts.of(session);
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            return new SimpleFormatter("circle",
                    new NText[]{
                            txt.ofStyled("⚪", NTextStyle.primary1()),
                            txt.ofStyled("⚫", NTextStyle.primary1()),
                    },
                    new NText[]{txt.ofStyled("⚫", NTextStyle.primary1())},
                    10, -1, 10, 10
            );
        });
        reg("parallelogram", session -> {
            NTexts txt = NTexts.of(session);
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            return new SimpleFormatter("parallelogram",
                    new NText[]{
                            txt.ofStyled("▱", NTextStyle.primary1()),
                            txt.ofStyled("▰", NTextStyle.primary1()),
                    },
                    new NText[]{txt.ofStyled("▰", NTextStyle.primary1())},
                    10, -1, 10, 10
            );
        });
        reg("simple", session -> {
            NTexts txt = NTexts.of(session);
            return new SimpleFormatter("simple",
                    new NText[]{
                            txt.ofStyled(" ", NTextStyle.primary1()),
                            txt.ofStyled("*", NTextStyle.primary1()),
                    },
                    new NText[]{txt.ofStyled("*", NTextStyle.primary1())},
                    null,
                    txt.ofStyled("[", NTextStyle.primary4()),
                    txt.ofStyled("]", NTextStyle.primary4()),
                    10, -1, 10, 10
            );
        });
        reg("clock", session -> {
            NTexts txt = NTexts.of(session);
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            //"\u25CB\u25D4\u25D1\u25D5\u25CF"
            // ○◔◑◕●
            return new SimpleFormatter("clock",
                    new NText[]{
                            txt.ofStyled("\u25CB", NTextStyle.primary1()),
                            txt.ofStyled("\u25D4", NTextStyle.primary1()),
                            txt.ofStyled("\u25D1", NTextStyle.primary1()),
                            txt.ofStyled("\u25D5", NTextStyle.primary1()),
                            txt.ofStyled("\u25CF", NTextStyle.primary1()),
                    },
                    new NText[]{txt.ofStyled("\u25CF", NTextStyle.primary1())}
                    , 1, 1, 10, 10
            );
        });
        reg("braille", session -> {
            NTexts txt = NTexts.of(session);
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            //"\u25CB\u25D4\u25D1\u25D5\u25CF"
            // ○◔◑◕●
            return new SimpleFormatter("braille",
                    new NText[]{
                            txt.ofStyled("\u2800", NTextStyle.primary1()),// ⠀
                            txt.ofStyled("\u2801", NTextStyle.primary1()),// ⠁
                            txt.ofStyled("\u2803", NTextStyle.primary1()),// ⠃
                            txt.ofStyled("\u2807", NTextStyle.primary1()),// ⠇
                            txt.ofStyled("\u2846", NTextStyle.primary1()),// ⡆
                            txt.ofStyled("\u28C4", NTextStyle.primary1()),// ⣄
                            txt.ofStyled("\u28E0", NTextStyle.primary1()),// ⣠
                            txt.ofStyled("\u28B0", NTextStyle.primary1()),// ⢰
                            txt.ofStyled("\u2838", NTextStyle.primary1()),// ⠸
                            txt.ofStyled("\u2819", NTextStyle.primary1()),// ⠙
                            txt.ofStyled("\u2819", NTextStyle.primary1()),// ⠙
                            txt.ofStyled("\u2809", NTextStyle.primary1()),// ⠉
                    },
                    new NText[]{
                            txt.ofStyled("\u2815", NTextStyle.primary1()),// ⠕
                            txt.ofStyled("\u2817", NTextStyle.primary1()),// ⠗
                    },
                    new NText[]{
                            txt.ofStyled("\u282A", NTextStyle.primary1()),// ⠪
                            txt.ofStyled("\u283A", NTextStyle.primary1()),// ⠺
                    }
                    , 1, 1, 10, 10
            );
        });
    }

    private static void reg(String name, Function<NSession, Formatter> f) {
        formatters.put(name, f);
    }

    public static CProgressBar of(NSession session) {
        return session.getOrComputeRefProperty(CProgressBar.class.getName(), CProgressBar::new);
    }

    public CProgressBar(NSession session) {
        this(session, -1);
    }

    public CProgressBar(NSession session, int determinateSize) {
        this.session = session;
        this.logger = NLog.of(CProgressBar.class, this.session);
        this.options = ProgressOptions.of(session);
        this.ws = session.getWorkspace();
        this.formatter = createFormatter(options.get("type").flatMap(NLiteral::asString).orElse(""), session);
        if (determinateSize <= 0) {
            determinateSize = options.get("size").flatMap(NLiteral::asInt).orElse(formatter.getDefaultWidth());
        }
        setDeterminateSize(determinateSize);
    }

    public String[] getFormatterNames() {
        return formatters.keySet().toArray(new String[0]);
    }

    public static Formatter createFormatter(String name, NSession session) {
        Function<NSession, Formatter> e = formatters.get(name);
        if (e != null) {
            Formatter u = e.apply(session);
            if (u != null) {
                return u;
            }
        }
        return formatters.get("").apply(session);
    }

    public long getMinPeriod() {
        return minPeriod;
    }

    public void setMinPeriod(long minPeriod) {
        this.minPeriod = minPeriod;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public CProgressBar setFormatter(String formatter) {
        return setFormatter(createFormatter(formatter, session));
    }

    public CProgressBar setFormatter(Formatter formatter) {
        this.formatter = formatter == null ? createFormatter("", session) : formatter;
        return this;
    }

    public IndeterminatePosition getIndeterminatePosition() {
        return indeterminatePosition;
    }

    public CProgressBar setIndeterminatePosition(IndeterminatePosition indeterminatePosition) {
        this.indeterminatePosition = indeterminatePosition == null ? DEFAULT_INDETERMINATE_POSITION : indeterminatePosition;
        return this;
    }

    public int getEffSize(boolean indeterminate) {
        int m = indeterminate ? formatter.getIndeterminateMaxWidth() : formatter.getMaxWidth();
        int s = indeterminate ? getIndeterminateSize() : getDeterminateSize();
        if (m > 0 && m < s) {
            return m;
        }
        return s;
    }

    public int getDeterminateSize() {
        return determinateSize;
    }

    public CProgressBar setDeterminateSize(int determinateSize) {
        this.determinateSize = determinateSize;
        return this;
    }

    public static class SimpleFormatter implements Formatter {

        private NText[] style;
        private NText[] intermediateForwardStyle;
        private NText[] intermediateBackwardStyle;
        private NText start;
        private NText end;
        private int defaultWidth;
        private int maxWidth;
        private int defaultIndeterminateWidth;
        private int maxIndeterminateWidth;
        private String name;

        public SimpleFormatter(String name, NText[] style, int defaultWidth, int maxWidth, int defaultIndeterminateWidth, int maxIndeterminateWidth) {
            this(name, style, null, null, null, null, defaultWidth, maxWidth, defaultIndeterminateWidth, maxIndeterminateWidth);
        }

        public SimpleFormatter(String name, NText[] style, NText[] forward, int defaultWidth, int maxWidth, int defaultIndeterminateWidth, int maxIndeterminateWidth) {
            this(name, style, forward, null, null, null, defaultWidth, maxWidth, defaultIndeterminateWidth, maxIndeterminateWidth);
        }

        public SimpleFormatter(String name, NText[] style, NText[] forward, NText[] backward, int defaultWidth, int maxWidth, int defaultIndeterminateWidth, int maxIndeterminateWidth) {
            this(name, style, forward, backward, null, null, defaultWidth, maxWidth, defaultIndeterminateWidth, maxIndeterminateWidth);
        }

        public SimpleFormatter(String name, NText[] style, NText[] forward, NText[] backward, NText start, NText end, int defaultWidth, int maxWidth, int defaultIndeterminateWidth, int maxIndeterminateWidth) {
            if (forward == null) {
                forward = style;
            }
            if (backward == null) {
                backward = forward;
            }
            this.name = name;
            this.defaultWidth = defaultWidth;
            this.maxWidth = maxWidth;
            this.style = style;
            this.intermediateForwardStyle = forward;
            this.intermediateBackwardStyle = backward;
            this.start = start;
            this.end = end;
            this.maxIndeterminateWidth = maxIndeterminateWidth;
            this.defaultIndeterminateWidth = defaultIndeterminateWidth;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        @Override
        public int getDefaultWidth() {
            return defaultWidth;
        }

        @Override
        public NText getIndicator(float itemDensity, int itemPosition) {
            return getIndicator(style, itemDensity, itemPosition);
        }

        public NText getIndicator(NText[] style, float itemDensity, int itemPosition) {
            int length = style.length;
            int p = (int) (itemDensity * length);
            if (p < 0) {
                p = 0;
            } else if (p >= length) {
                p = length - 1;
            }
            return style[p];
        }

        @Override
        public NText getIntermediateIndicator(int intermediatePosition, int indeterminateSize, int intermediateStartPosition, boolean forward) {
            float density = (intermediatePosition * 1.f / indeterminateSize);
            if (forward) {
                int length = intermediateForwardStyle.length;
                int p = (int) (density * length);
                if (p < 0) {
                    p = 0;
                } else if (p >= length) {
                    p = length - 1;
                }
                return intermediateForwardStyle[p];
            } else {
                //should be symmetric to farward
                density = density;
                int length = intermediateBackwardStyle.length;
                int p = (int) (density * length);
                if (p < 0) {
                    p = 0;
                } else if (p >= length) {
                    p = length - 1;
                }
                return intermediateBackwardStyle[p];
            }
        }

        @Override
        public NText getStart() {
            return start;
        }

        @Override
        public NText getEnd() {
            return end;
        }

        @Override
        public int getIndeterminateMaxWidth() {
            return maxIndeterminateWidth;
        }

        @Override
        public int getDefaultIndeterminateWidth() {
            return defaultIndeterminateWidth;
        }
    }

    public static interface Formatter {

        NText getIntermediateIndicator(int intermediatePosition, int indeterminateSize, int itemPosition, boolean forward);

        NText getIndicator(float itemDensity, int itemPosition);

        NText getStart();

        NText getEnd();

        int getMaxWidth();

        int getDefaultWidth();

        int getIndeterminateMaxWidth();

        int getDefaultIndeterminateWidth();
    }

    public int getColumns() {
        return columns;
    }

    public CProgressBar setColumns(int columns) {
        this.columns = columns <= 1 ? 1 : columns;
        return this;
    }

    public boolean isSuffixMoveLineStart() {
        return suffixMoveLineStart;
    }

    public boolean isPrefixMoveLineStart() {
        return prefixMoveLineStart;
    }

    public boolean isNoMoveLineStart() {
        return !isPrefixMoveLineStart() && !isSuffixMoveLineStart();
    }

    public CProgressBar setSuffixMoveLineStart(boolean v) {
        this.suffixMoveLineStart = v;
        return this;
    }

    public CProgressBar setPrefixMoveLineStart(boolean v) {
        this.prefixMoveLineStart = v;
        return this;
    }

    public CProgressBar setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    public NText progress(int percent) {
        long now = System.currentTimeMillis();
        NTexts txt = NTexts.of(session);
        if (now < lastPrint + minPeriod) {
            return txt.ofPlain("");
        }
        lastPrint = now;
        boolean indeterminate = percent < 0;
        int eSize = getEffSize(indeterminate);
        if (indeterminate) {
            NTextBuilder formattedLine = txt.ofBuilder();
            formattedLine.append(getFormatter().getStart());
            int indeterminateSize = (int) (this.indeterminateRatio * eSize);
            boolean forward = true;
            if (indeterminateSize >= eSize) {
                indeterminateSize = eSize - 1;
            }
            if (indeterminateSize < 1) {
                indeterminateSize = 1;
            }
            int x = 0;
            if (indeterminateSize < eSize) {
                int p = eSize - indeterminateSize;
                int h = indeterminatePosition.evalIndeterminatePos(this, 2 * p);
                if (h < 0) {
                    h = -h;
                }
                x = h % (2 * p);//(int) ((s * 2 * size) / 60.0);
                if (x >= p) {
                    forward = false;
                    x = 2 * p - x;
                }
            } else {
                x = 0;
            }

            if (x < 0) {
                x = 0;
            }

            for (int i = 0; i < x; i++) {
                formattedLine.append(getFormatter().getIndicator(0, i));
            }
            for (int i = 0; i < indeterminateSize; i++) {
                formattedLine.append(getFormatter().getIntermediateIndicator(i, indeterminateSize, x, forward));
            }
            int r = eSize - x - indeterminateSize;

            for (int i = 0; i < r; i++) {
                formattedLine.append(getFormatter().getIndicator(0, x + indeterminateSize + i));
            }
            formattedLine.append(getFormatter().getEnd());
            return formattedLine.build();
        } else {
            if (percent > 100) {
                percent = 100 - percent;
            }
            double d = (eSize / 100.0 * percent);
            int x = (int) d;
            float rest = (float) (d - x);
            NTextBuilder formattedLine = txt.ofBuilder();
            formattedLine.append(getFormatter().getStart());
            if (x > 0) {
                for (int i = 0; i < x; i++) {
                    formattedLine.append(getFormatter().getIndicator(1, i));
                }
            }
            if (rest > 0 && (eSize - x) > 0) {
                formattedLine.append(getFormatter().getIndicator(rest, x));
                for (int i = 0; i < eSize - x - 1; i++) {
                    formattedLine.append(getFormatter().getIndicator(0, x + 1 + i));
                }
            } else {
                for (int i = 0; i < eSize - x; i++) {
                    formattedLine.append(getFormatter().getIndicator(0, x + i));
                }
            }
            formattedLine.append(getFormatter().getEnd());
            return formattedLine.build();
        }
    }

    public void printProgress(int percent, NText msg, NPrintStream out) {
        NText p = progress(percent, msg);
        if (p == null || p.isEmpty()) {
            return;
        }
        Level armedLogLevel = options.getArmedLogLevel();
        if (armedLogLevel != null) {
            logger.with().verb(NLogVerb.PROGRESS)
                    .level(armedLogLevel)
                    .log(NMsg.ofNtf(p));
        } else {
            synchronized (CProgressBar.class) {
                out.resetLine();
                out.print(p);
            }
        }
    }
    public void printProgress2(NText p, NPrintStream out) {
        if (p == null || p.isEmpty()) {
            return;
        }
        Level armedLogLevel = options.getArmedLogLevel();
        if (options.isArmedNewline()) {
            out.print("\n");
        }else if (armedLogLevel!=null) {
            logger.with().verb(NLogVerb.PROGRESS)
                    .level(armedLogLevel)
                    .log(NMsg.ofNtf(p));
        }else{
            synchronized (CProgressBar.class) {
                out.resetLine();
                out.print(p);
            }
        }
    }

    public NText progress(int percent, NText msg) {
        NTexts txt = NTexts.of(session);
        NTextBuilder sb = txt.ofBuilder();
        if (maxMessage < columns) {
            maxMessage = columns;
        }
        int s2 = 0;
        if (msg == null) {
            msg = txt.ofPlain("");
        }
        s2 = msg.textLength();
        Level armedLogLevel = options.getArmedLogLevel();
        if (armedLogLevel == null) {
            if (isPrefixMoveLineStart()) {
                if (options.isArmedNewline()) {
                    if (!isSuffixMoveLineStart()) {
                        sb.append("\n");
                    }
                } else {
                    sb.append(txt.ofCommand(NTerminalCommand.CLEAR_LINE));
                    sb.append(txt.ofCommand(NTerminalCommand.MOVE_LINE_START));
                }
            }
        }
        NText p = progress(percent);
        if (p == null) {
            return txt.ofBlank();
        }
        sb.append(p).append(" ");
        sb.append(msg);
        sb.append(CoreStringUtils.fillString(' ', maxMessage - s2));
        if (isSuffixMoveLineStart()) {
            if (armedLogLevel == null && options.isArmedNewline()) {
                sb.append("\n");
            }
        }
        if (maxMessage < s2) {
            maxMessage = s2;
        }
        if (maxMessage > maxColumns) {
            maxMessage = maxColumns;
        }
        return sb.build();
    }

    public interface IndeterminatePosition {

        int evalIndeterminatePos(CProgressBar bar, int size);
    }

    public static class DefaultIndeterminatePosition implements IndeterminatePosition {

        @Override
        public int evalIndeterminatePos(CProgressBar bar, int size) {
            int ss = 2 * Calendar.getInstance().get(Calendar.SECOND);
            int ms = Calendar.getInstance().get(Calendar.MILLISECOND);
            if (ms > 500) {
                ss += 1;
            }
            return ss;
        }
    }

    public float getIndeterminateRatio() {
        return indeterminateRatio;
    }

    public CProgressBar setIndeterminateRatio(float indeterminateRatio) {
        if (indeterminateRatio <= 0 || indeterminateRatio >= 1) {
            indeterminateRatio = 0.3f;
        }
        this.indeterminateRatio = indeterminateRatio;
        return this;
    }

    public int getIndeterminateSize() {
        return indeterminateSize;
    }

    public CProgressBar setIndeterminateSize(int indeterminateSize) {
        this.indeterminateSize = indeterminateSize;
        return this;
    }
}
