package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.text.*;

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
    private NutsSession session;
    private int columns = 3;
    private int maxColumns = 133;
    private boolean suffixMoveLineStart = true;
    private boolean prefixMoveLineStart = true;
    private long lastPrint = 0;
    private long minPeriod = 0;
    private IndeterminatePosition indeterminatePosition = DEFAULT_INDETERMINATE_POSITION;
    private boolean optionNewline;
    private Formatter formatter;
    private NutsWorkspace ws;
    private static Map<String, Function<NutsSession, Formatter>> formatters = new HashMap();

    static {
        reg("",session-> {
            NutsTexts txt = NutsTexts.of(session);
            return CorePlatformUtils.SUPPORTS_UTF_ENCODING ? createFormatter("braille", session) : createFormatter("simple", session);
        });
        reg( "square",session-> {
            NutsTexts txt = NutsTexts.of(session);
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            return new SimpleFormatter("square",
                    new NutsText[]{
                            txt.ofStyled("⬜", NutsTextStyle.primary1()),
                            txt.ofStyled("⬛", NutsTextStyle.primary1()),
                    },
                    new NutsText[]{txt.ofStyled("⬛", NutsTextStyle.primary1())},
                    10,
                    -1,10,10
            );
        });
        reg( "vbar",session-> {
            //" ▁▂▃▄▅▆▇█"
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            NutsTexts txt = NutsTexts.of(session);
            return new SimpleFormatter("vbar",
                    new NutsText[]{
                            txt.ofStyled(" ", NutsTextStyle.primary1()),
                            txt.ofStyled("▁", NutsTextStyle.primary1()),
                            txt.ofStyled("▂", NutsTextStyle.primary1()),
                            txt.ofStyled("▃", NutsTextStyle.primary1()),
                            txt.ofStyled("▄", NutsTextStyle.primary1()),
                            txt.ofStyled("▅", NutsTextStyle.primary1()),
                            txt.ofStyled("▆", NutsTextStyle.primary1()),
                            txt.ofStyled("▇", NutsTextStyle.primary1()),
                            txt.ofStyled("█", NutsTextStyle.primary1()),
                    },
                    new NutsText[]{
                            txt.ofStyled("▁", NutsTextStyle.primary1()),
                            txt.ofStyled("▂", NutsTextStyle.primary1()),
                            txt.ofStyled("▃", NutsTextStyle.primary1()),
                            txt.ofStyled("▄", NutsTextStyle.primary1()),
                            txt.ofStyled("▅", NutsTextStyle.primary1()),
                            txt.ofStyled("▆", NutsTextStyle.primary1()),
                            txt.ofStyled("▇", NutsTextStyle.primary1()),
                            txt.ofStyled("█", NutsTextStyle.primary1()),
                    },
                    new NutsText[]{
                            txt.ofStyled("█", NutsTextStyle.primary1()),
                            txt.ofStyled("▇", NutsTextStyle.primary1()),
                            txt.ofStyled("▆", NutsTextStyle.primary1()),
                            txt.ofStyled("▅", NutsTextStyle.primary1()),
                            txt.ofStyled("▄", NutsTextStyle.primary1()),
                            txt.ofStyled("▃", NutsTextStyle.primary1()),
                            txt.ofStyled("▂", NutsTextStyle.primary1()),
                            txt.ofStyled("▁", NutsTextStyle.primary1()),
                    },
                    1, 1,10,10
            );
        });

        reg( "shadow",session-> {
            NutsTexts txt = NutsTexts.of(session);
            //" ░▒▓█"
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            return new SimpleFormatter("shadow",
                    new NutsText[]{
                            txt.ofStyled(" ", NutsTextStyle.primary1()),
                            txt.ofStyled("░", NutsTextStyle.primary1()),
                            txt.ofStyled("▒", NutsTextStyle.primary1()),
                            txt.ofStyled("▓", NutsTextStyle.primary1()),
                            txt.ofStyled("█", NutsTextStyle.primary1()),
                    },
                    new NutsText[]{
                            txt.ofStyled("█", NutsTextStyle.primary1()),
                    },
                    1, 1,10,10
            );
        });
        reg( "hbar",session-> {
            NutsTexts txt = NutsTexts.of(session);
            //" ▏▎▍▌▋▊▉█"
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            return new SimpleFormatter("hbar",
                    new NutsText[]{
                            txt.ofStyled(" ", NutsTextStyle.primary1()),
                            txt.ofStyled("▏", NutsTextStyle.primary1()),
                            txt.ofStyled("▎", NutsTextStyle.primary1()),
                            txt.ofStyled("▍", NutsTextStyle.primary1()),
                            txt.ofStyled("▌", NutsTextStyle.primary1()),
                            txt.ofStyled("▋", NutsTextStyle.primary1()),
                            txt.ofStyled("▊", NutsTextStyle.primary1()),
                            txt.ofStyled("▉", NutsTextStyle.primary1()),
                            txt.ofStyled("█", NutsTextStyle.primary1()),
                    },
                    new NutsText[]{
                            txt.ofStyled("█", NutsTextStyle.primary1()),
                    },
                    10, -1,10,10
            );
        });
        reg( "circle",session-> {
            NutsTexts txt = NutsTexts.of(session);
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            return new SimpleFormatter("circle",
                    new NutsText[]{
                            txt.ofStyled("⚪", NutsTextStyle.primary1()),
                            txt.ofStyled("⚫", NutsTextStyle.primary1()),
                    },
                    new NutsText[]{txt.ofStyled("⚫", NutsTextStyle.primary1())},
                    10, -1,10,10
            );
        });
        reg( "parallelogram",session-> {
            NutsTexts txt = NutsTexts.of(session);
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            return new SimpleFormatter("parallelogram",
                    new NutsText[]{
                            txt.ofStyled("▱", NutsTextStyle.primary1()),
                            txt.ofStyled("▰", NutsTextStyle.primary1()),
                    },
                    new NutsText[]{txt.ofStyled("▰", NutsTextStyle.primary1())},
                    10, -1,10,10
            );
        });
        reg( "simple",session-> {
            NutsTexts txt = NutsTexts.of(session);
            return new SimpleFormatter("simple",
                    new NutsText[]{
                            txt.ofStyled(" ", NutsTextStyle.primary1()),
                            txt.ofStyled("*", NutsTextStyle.primary1()),
                    },
                    new NutsText[]{txt.ofStyled("*", NutsTextStyle.primary1())},
                    null,
                    txt.ofStyled("[", NutsTextStyle.primary4()),
                    txt.ofStyled("]", NutsTextStyle.primary4()),
                    10, -1,10,10
            );
        });
        reg( "clock",session-> {
            NutsTexts txt = NutsTexts.of(session);
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            //"\u25CB\u25D4\u25D1\u25D5\u25CF"
            // ○◔◑◕●
            return new SimpleFormatter("clock",
                    new NutsText[]{
                            txt.ofStyled("\u25CB", NutsTextStyle.primary1()),
                            txt.ofStyled("\u25D4", NutsTextStyle.primary1()),
                            txt.ofStyled("\u25D1", NutsTextStyle.primary1()),
                            txt.ofStyled("\u25D5", NutsTextStyle.primary1()),
                            txt.ofStyled("\u25CF", NutsTextStyle.primary1()),
                    },
                    new NutsText[]{txt.ofStyled("\u25CF", NutsTextStyle.primary1())}
                    , 1, 1,10,10
            );
        });
        reg( "braille",session-> {
            NutsTexts txt = NutsTexts.of(session);
            if (!CorePlatformUtils.SUPPORTS_UTF_ENCODING) {
                return null;
            }
            //"\u25CB\u25D4\u25D1\u25D5\u25CF"
            // ○◔◑◕●
            return new SimpleFormatter("braille",
                    new NutsText[]{
                            txt.ofStyled("\u2800", NutsTextStyle.primary1()),// ⠀
                            txt.ofStyled("\u2801", NutsTextStyle.primary1()),// ⠁
                            txt.ofStyled("\u2803", NutsTextStyle.primary1()),// ⠃
                            txt.ofStyled("\u2807", NutsTextStyle.primary1()),// ⠇
                            txt.ofStyled("\u2846", NutsTextStyle.primary1()),// ⡆
                            txt.ofStyled("\u28C4", NutsTextStyle.primary1()),// ⣄
                            txt.ofStyled("\u28E0", NutsTextStyle.primary1()),// ⣠
                            txt.ofStyled("\u28B0", NutsTextStyle.primary1()),// ⢰
                            txt.ofStyled("\u2838", NutsTextStyle.primary1()),// ⠸
                            txt.ofStyled("\u2819", NutsTextStyle.primary1()),// ⠙
                            txt.ofStyled("\u2819", NutsTextStyle.primary1()),// ⠙
                            txt.ofStyled("\u2809", NutsTextStyle.primary1()),// ⠉
                    },
                    new NutsText[]{
                            txt.ofStyled("\u2815", NutsTextStyle.primary1()),// ⠕
                            txt.ofStyled("\u2817", NutsTextStyle.primary1()),// ⠗
                    },
                    new NutsText[]{
                            txt.ofStyled("\u282A", NutsTextStyle.primary1()),// ⠪
                            txt.ofStyled("\u283A", NutsTextStyle.primary1()),// ⠺
                    }
                    , 1, 1,10,10
            );
        });
    }

    private static void reg(String name, Function<NutsSession, Formatter> f) {
        formatters.put(name, f);
    }

    public static CProgressBar of(NutsSession session) {
        return session.getOrComputeRefProperty(CProgressBar.class.getName(), CProgressBar::new);
    }

    public CProgressBar(NutsSession session) {
        this(session, -1);
    }

    public CProgressBar(NutsSession session, int determinateSize) {
        this.session = session;
        ProgressOptions o = ProgressOptions.of(session);
        this.optionNewline = o.isArmedNewline();
        this.ws = session.getWorkspace();
        this.formatter = createFormatter(o.get("type").flatMap(NutsValue::asString).orElse(""), session);
        if (determinateSize <= 0) {
            determinateSize = o.get("size").flatMap(NutsValue::asInt).orElse(formatter.getDefaultWidth());
        }
        setDeterminateSize(determinateSize);
    }

    public String[] getFormatterNames() {
        return formatters.keySet().toArray(new String[0]);
    }

    public static Formatter createFormatter(String name, NutsSession session) {
        Function<NutsSession, Formatter> e = formatters.get(name);
        if(e!=null){
            Formatter u = e.apply(session);
            if(u!=null) {
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

        private NutsText[] style;
        private NutsText[] intermediateForwardStyle;
        private NutsText[] intermediateBackwardStyle;
        private NutsText start;
        private NutsText end;
        private int defaultWidth;
        private int maxWidth;
        private int defaultIndeterminateWidth;
        private int maxIndeterminateWidth;
        private String name;

        public SimpleFormatter(String name, NutsText[] style, int defaultWidth, int maxWidth, int defaultIndeterminateWidth, int maxIndeterminateWidth) {
            this(name, style, null, null, null, null, defaultWidth, maxWidth,defaultIndeterminateWidth, maxIndeterminateWidth);
        }

        public SimpleFormatter(String name, NutsText[] style, NutsText[] forward, int defaultWidth, int maxWidth, int defaultIndeterminateWidth, int maxIndeterminateWidth) {
            this(name, style, forward, null, null, null, defaultWidth, maxWidth,defaultIndeterminateWidth, maxIndeterminateWidth);
        }

        public SimpleFormatter(String name, NutsText[] style, NutsText[] forward, NutsText[] backward, int defaultWidth, int maxWidth, int defaultIndeterminateWidth, int maxIndeterminateWidth) {
            this(name, style, forward, backward, null, null, defaultWidth, maxWidth,defaultIndeterminateWidth, maxIndeterminateWidth);
        }

        public SimpleFormatter(String name, NutsText[] style, NutsText[] forward, NutsText[] backward, NutsText start, NutsText end, int defaultWidth, int maxWidth, int defaultIndeterminateWidth, int maxIndeterminateWidth) {
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
            this.maxIndeterminateWidth =maxIndeterminateWidth;
            this.defaultIndeterminateWidth =defaultIndeterminateWidth;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        @Override
        public int getDefaultWidth() {
            return defaultWidth;
        }

        @Override
        public NutsText getIndicator(float itemDensity, int itemPosition) {
            return getIndicator(style, itemDensity, itemPosition);
        }

        public NutsText getIndicator(NutsText[] style, float itemDensity, int itemPosition) {
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
        public NutsText getIntermediateIndicator(int intermediatePosition, int indeterminateSize, int intermediateStartPosition, boolean forward) {
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
        public NutsText getStart() {
            return start;
        }

        @Override
        public NutsText getEnd() {
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

        NutsText getIntermediateIndicator(int intermediatePosition, int indeterminateSize, int itemPosition, boolean forward);

        NutsText getIndicator(float itemDensity, int itemPosition);

        NutsText getStart();

        NutsText getEnd();

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

    public CProgressBar setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    public NutsText progress(int percent) {
        long now = System.currentTimeMillis();
        NutsTexts txt = NutsTexts.of(session);
        if (now < lastPrint + minPeriod) {
            return txt.ofPlain("");
        }
        lastPrint = now;
        boolean indeterminate = percent < 0;
        int eSize = getEffSize(indeterminate);
        if (indeterminate) {
            NutsTextBuilder formattedLine = txt.ofBuilder();
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
            NutsTextBuilder formattedLine = txt.ofBuilder();
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

    public void printProgress(int percent, NutsText msg, NutsPrintStream out) {
        NutsText p = progress(percent, msg);
        if (p == null || p.isEmpty()) {
            return;
        }
        out.print(p);
    }

    public NutsText progress(int percent, NutsText msg) {
        NutsTexts txt = NutsTexts.of(session);
        NutsTextBuilder sb = txt.ofBuilder();
        if (maxMessage < columns) {
            maxMessage = columns;
        }
        int s2 = 0;
        if (msg == null) {
            msg = txt.ofPlain("");
        }
        s2 = msg.textLength();
        if (isPrefixMoveLineStart()) {
            if (optionNewline) {
                if (!isSuffixMoveLineStart()) {
                    sb.append("\n");
                }
            } else {
                sb.append(txt.ofCommand(NutsTerminalCommand.CLEAR_LINE));
                sb.append(txt.ofCommand(NutsTerminalCommand.MOVE_LINE_START));
            }
        }
        NutsText p = progress(percent);
        if (p == null) {
            return txt.ofBlank();
        }
        sb.append(p).append(" ");
        sb.append(msg);
        sb.append(CoreStringUtils.fillString(' ', maxMessage - s2));
        if (isSuffixMoveLineStart()) {
            if (optionNewline) {
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
