package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.text.FPrintCommands;

import java.util.Calendar;

import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;

/**
 *
 * inspired by
 * https://github.com/Changaco/unicode-progress-bars/blob/master/generator.html
 *
 * @author thevpc
 */
public class CProgressBar {

    public static final Formatter RECTANGLES = new SimpleFormatter("⬜⬛", "⬛", null, null, null);
    public static final Formatter CIRCLES = new SimpleFormatter("⚪⚫", "⚫", null, null, null);
    public static final Formatter PARALLELOGRAM = new SimpleFormatter("▱▰", "▰", null, null, null);
    public static final Formatter SIMPLE = new SimpleFormatter(" *", null, null, "[", "]");
    public static final Formatter RECTANGLES2 = new SimpleFormatter(" ▁▂▃▄▅▆▇█", "▁▂▃▄▅▆▇█", "█▇▆▅▄▃▂▁", null, null);
    public static final Formatter RECTANGLES3 = new SimpleFormatter(" ░▒▓█", "█", null, null, null);
    public static final Formatter RECTANGLES4 = new SimpleFormatter(" ▏▎▍▌▋▊▉█", "▁▂▃▄▅▆▇█", "█▇▆▅▄▃▂▁", null, null);
    public static final Formatter DOTS1 = new SimpleFormatter(" ⣀⣄⣤⣦⣶⣷⣿", "⣿", null, null, null);
    public static final Formatter DOTS2 = new SimpleFormatter(" ⣀⣄⣆⣇⣧⣷⣿", "⣿", null, null, null);
    public static final Formatter CIRCLES2 = new SimpleFormatter("○◔◐◕⬤", "⬤", null, null, null);
    public static final Formatter DEFAULT = RECTANGLES4;
    private static final IndeterminatePosition DEFAULT_INDETERMINATE_POSITION = new DefaultIndeterminatePosition();
    private boolean formatted = true;
    private int size = 10;
    private int maxMessage = 0;
    private float indeterminateSize = 0.3f;
    private NutsSession session;
    private int columns = 3;
    private int maxColumns = 133;
    private boolean suffixMoveLineStart = true;
    private boolean prefixMoveLineStart = true;
    private long lastPrint = 0;
    private long minPeriod = 0;
    private IndeterminatePosition indeterminatePosition = DEFAULT_INDETERMINATE_POSITION;
    private boolean optionNewline;
    private Formatter formatter = CorePlatformUtils.SUPPORTS_UTF_ENCODING ?RECTANGLES4:SIMPLE;
    private NutsWorkspace ws;

    public CProgressBar(NutsSession session) {
        this.session = session;
        formatted = session != null;
        if (session != null) {
            optionNewline = CoreNutsUtils.parseProgressOptions(session).isArmedNewline();
            ws=session.getWorkspace();
        }
    }

    public CProgressBar(NutsSession session, int size) {
        this.session = session;
        setSize(size);
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

    public CProgressBar setFormatter(Formatter formatter) {
        this.formatter = formatter == null ? DEFAULT : formatter;
        return this;
    }

    public IndeterminatePosition getIndeterminatePosition() {
        return indeterminatePosition;
    }

    public CProgressBar setIndeterminatePosition(IndeterminatePosition indeterminatePosition) {
        this.indeterminatePosition = indeterminatePosition == null ? DEFAULT_INDETERMINATE_POSITION : indeterminatePosition;
        return this;
    }

    public int getSize() {
        return size;
    }

    public CProgressBar setSize(int size) {
        if (size < 10) {
            size = 10;
        }
        this.size = size;
        return this;
    }

    public static class SimpleFormatter implements Formatter {

        private String style;
        private String intermediateForwardStyle;
        private String intermediateBackwardStyle;
        private String start;
        private String end;

        public SimpleFormatter(String style, String forward, String backward, String start, String end) {
            if (forward == null || forward.isEmpty()) {
                forward = style;
            }
            if (backward == null || backward.isEmpty()) {
                backward = forward;
            }
            this.style = style;
            this.intermediateForwardStyle = forward;
            this.intermediateBackwardStyle = backward;
            this.start = start == null ? "" : start;
            this.end = end == null ? "" : end;
        }

        @Override
        public String getIndicator(float itemDensity, int itemPosition) {
            return getIndicator(style, itemDensity, itemPosition);
        }

        public String getIndicator(String style, float itemDensity, int itemPosition) {
            int length = style.length();
            int p = (int) (itemDensity * length);
            if (p < 0) {
                p = 0;
            } else if (p >= length) {
                p = length - 1;
            }
            return String.valueOf(style.charAt(p));
        }

        @Override
        public String getIntermediateIndicator(int intermediatePosition, int indeterminateSize, int intermediateStartPosition, boolean forward) {

//            int halfSize = indeterminateSize / 2;
//            int away = intermediatePosition - halfSize;
//            if (away < 0) {
//                away = -away;
//            }
//            float density = ((1.f - away) / halfSize) / 2.0f + 0.5f;
            float density = (intermediatePosition * 1.f / indeterminateSize);
            if (forward) {
                int length = intermediateForwardStyle.length();
                int p = (int) (density * length);
                if (p < 0) {
                    p = 0;
                } else if (p >= length) {
                    p = length - 1;
                }
                return String.valueOf(intermediateForwardStyle.charAt(p));
            } else {
                //should be symmetric to farward
                density = density;
                int length = intermediateBackwardStyle.length();
                int p = (int) (density * length);
                if (p < 0) {
                    p = 0;
                } else if (p >= length) {
                    p = length - 1;
                }
                return String.valueOf(intermediateBackwardStyle.charAt(p));
            }
        }

        @Override
        public String getStart() {
            return start;
        }

        @Override
        public String getEnd() {
            return end;
        }

    }

    public static interface Formatter {

        public String getIntermediateIndicator(int intermediatePosition, int indeterminateSize, int itemPosition, boolean forward);

        public String getIndicator(float itemDensity, int itemPosition);

        public String getStart();

        public String getEnd();
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

    public String progress(int percent) {
        if (session != null) {
            return progressWithSession(percent);
        }
        return progressWithoutSession(percent);
    }

    public String progressWithoutSession(int percent) {
        long now = System.currentTimeMillis();
        if (minPeriod > 0 && now < lastPrint + minPeriod) {
            return "";
        }
        lastPrint = now;
        boolean indeterminate = percent < 0;
        if (indeterminate) {
            StringBuilder formattedLine = new StringBuilder();
            formattedLine.append(getFormatter().getStart());
            int indeterminateSize = (int) (this.indeterminateSize * size);
            if (indeterminateSize >= size) {
                indeterminateSize = size - 1;
            }
            if (indeterminateSize < 1) {
                indeterminateSize = 1;
            }
            int x = 0;
            boolean forward = true;
            if (indeterminateSize < size) {
                int p = this.size - indeterminateSize;
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
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < x; i++) {
                sb.append(getFormatter().getIndicator(0, i));
            }
            for (int i = 0; i < indeterminateSize; i++) {
                sb.append(getFormatter().getIntermediateIndicator(i, indeterminateSize, x, forward));
            }
            formattedLine.append(sb.toString());
            int r = size - x - indeterminateSize;
            sb.setLength(0);
            for (int i = 0; i < r; i++) {
                sb.append(getFormatter().getIndicator(0, x + indeterminateSize + i));
            }
            formattedLine.append(sb.toString());
            formattedLine.append(getFormatter().getEnd());
            return formattedLine.toString();
        } else {
            if (percent > 100) {
                percent = 100 - percent;
            }
            double d = (size / 100.0 * percent);
            int x = (int) d;
            float rest = (float) (d - x);
            StringBuilder formattedLine = new StringBuilder();
            formattedLine.append(getFormatter().getStart());
            if (x > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < x; i++) {
                    sb.append(getFormatter().getIndicator(1, i));
                }
                formattedLine.append(sb.toString());
            }
            StringBuilder sb = new StringBuilder();
            if (rest > 0 && (size - x) > 0) {
                sb.append(getFormatter().getIndicator(rest, x));
                for (int i = 0; i < size - x - 1; i++) {
                    sb.append(getFormatter().getIndicator(0, x + 1 + i));
                }
            } else {
                for (int i = 0; i < size - x; i++) {
                    sb.append(getFormatter().getIndicator(0, x + i));
                }
            }
            formattedLine.append(sb.toString());
            formattedLine.append(getFormatter().getEnd());
            return formattedLine.toString();
        }
    }

    public String progressWithSession(int percent) {
        return progressWithSessionOld(percent);
//        return progressWithSessionNew(percent);
    }

    public String progressWithSessionOld(int percent) {
        long now = System.currentTimeMillis();
        if (now < lastPrint + minPeriod) {
            return "";
        }
        lastPrint = now;
        boolean indeterminate = percent < 0;
        if (indeterminate) {
            NutsTextBuilder formattedLine = NutsTexts.of(session).builder();
            formattedLine.append(getFormatter().getStart());
            int indeterminateSize = (int) (this.indeterminateSize * size);
            boolean forward = true;
            if (indeterminateSize >= size) {
                indeterminateSize = size - 1;
            }
            if (indeterminateSize < 1) {
                indeterminateSize = 1;
            }
            int x = 0;
            if (indeterminateSize < size) {
                int p = this.size - indeterminateSize;
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
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < x; i++) {
                sb.append(getFormatter().getIndicator(0, i));
            }
            for (int i = 0; i < indeterminateSize; i++) {
                sb.append(getFormatter().getIntermediateIndicator(i, indeterminateSize, x, forward));
            }
            formattedLine.append(sb.toString(), NutsTextStyle.primary1());
            int r = size - x - indeterminateSize;
            sb.setLength(0);
            for (int i = 0; i < r; i++) {
                sb.append(getFormatter().getIndicator(0, x + indeterminateSize + i));
            }
            formattedLine.append(sb.toString());
            formattedLine.append(getFormatter().getEnd());
            return formattedLine.toString();
        } else {
            if (percent > 100) {
                percent = 100 - percent;
            }
            double d = (size / 100.0 * percent);
            int x = (int) d;
            float rest = (float) (d - x);
            NutsTextBuilder formattedLine = NutsTexts.of(session).builder();
            formattedLine.append(getFormatter().getStart());
            if (x > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < x; i++) {
                    sb.append(getFormatter().getIndicator(1, i));
                }
                formattedLine.append(sb.toString(), NutsTextStyle.primary1());
            }
            StringBuilder sb = new StringBuilder();
            if (rest > 0 && (size - x) > 0) {
                sb.append(getFormatter().getIndicator(rest, x));
                for (int i = 0; i < size - x - 1; i++) {
                    sb.append(getFormatter().getIndicator(0, x + 1 + i));
                }
            } else {
                for (int i = 0; i < size - x; i++) {
                    sb.append(getFormatter().getIndicator(0, x + i));
                }
            }
            formattedLine.append(sb.toString(), NutsTextStyle.primary1());
            formattedLine.append(getFormatter().getEnd());
            return formattedLine.toString();
        }
    }

    public String progressWithSessionNew(int percent) {
        long now = System.currentTimeMillis();
        if (now < lastPrint + minPeriod) {
            return "";
        }
        lastPrint = now;
        boolean indeterminate = percent < 0;
        if (indeterminate) {
            NutsTextBuilder formattedLine = NutsTexts.of(session).builder();
            formattedLine.append(getFormatter().getStart());
            int indeterminateSize = (int) (this.indeterminateSize * size);
            boolean forward = true;
            if (indeterminateSize >= size) {
                indeterminateSize = size - 1;
            }
            if (indeterminateSize < 1) {
                indeterminateSize = 1;
            }
            int x = 0;
            if (indeterminateSize < size) {
                int p = this.size - indeterminateSize;
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
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < x; i++) {
                sb.append(getFormatter().getIndicator(0, i));
            }
            for (int i = 0; i < indeterminateSize; i++) {
                sb.append(getFormatter().getIntermediateIndicator(i, indeterminateSize, x, forward));
            }
            formattedLine.append(sb.toString(), NutsTextStyle.primary1());
            int r = size - x - indeterminateSize;
            sb.setLength(0);
            for (int i = 0; i < r; i++) {
                sb.append(getFormatter().getIndicator(0, x + indeterminateSize + i));
            }
            formattedLine.append(sb.toString());
            formattedLine.append(getFormatter().getEnd());
            return formattedLine.toString();
        } else {
            if (percent > 100) {
                percent = 100 - percent;
            }
            double d = (size / 100.0 * percent);
            int x = (int) d;
            float rest = (float) (d - x);
            NutsTextBuilder formattedLine = NutsTexts.of(session).builder();
            formattedLine.append(getFormatter().getStart());
            StringBuilder sb = new StringBuilder();
            if (x > 0) {
                for (int i = 0; i < x; i++) {
                    sb.append(getFormatter().getIndicator(1, i));
                }
            }
            if (rest > 0 && (size - x) > 0) {
                sb.append(getFormatter().getIndicator(rest, x));
                for (int i = 0; i < size - x - 1; i++) {
                    sb.append(getFormatter().getIndicator(0, x + 1 + i));
                }
            } else {
                for (int i = 0; i < size - x; i++) {
                    sb.append(getFormatter().getIndicator(0, x + i));
                }
            }
            formattedLine.append(sb.toString(), NutsTextStyle.primary1());
            formattedLine.append(getFormatter().getEnd());
            return formattedLine.toString();
        }
    }

    public void printProgress(int percent, String msg, NutsPrintStream out) {
        String p = progress(percent, msg);
        if (p == null || p.isEmpty()) {
            return;
        }
        out.print(p);
    }

    public String progress(int percent, String msg) {
        StringBuilder sb = new StringBuilder();
        if (maxMessage < columns) {
            maxMessage = columns;
        }
        int s2 = 0;
        if (formatted) {
            if (msg == null) {
                msg = "";
            }
            s2 = session == null ? msg.length() : NutsTexts.of(session).builder().append(msg).textLength();
            if (isPrefixMoveLineStart()) {
                if (optionNewline) {
                    if (!isSuffixMoveLineStart()) {
                        sb.append("\n");
                    }
                } else {
                    FPrintCommands.runMoveLineStart(sb);
                }
            }
            String p = progress(percent);
            if (p == null || p.isEmpty()) {
                return "";
            }
            sb.append(p).append(" ");
            sb.append(msg);
            sb.append(CoreStringUtils.fillString(' ', maxMessage - s2));
//            sb.append(" ");
//            sb.append(maxMessage);
//            if(maxMessage<s2){
//                maxMessage=s2;
//            }
//            sb.append(" ");
//            sb.append(maxMessage);
            if (isSuffixMoveLineStart()) {
                if (optionNewline) {
                    sb.append("\n");
                } else {
                    FPrintCommands.runLaterResetLine(sb);
                }
            }
        } else {
            s2 = msg.length();
            String p = progress(percent);
            if (p == null || p.isEmpty()) {
                return "";
            }
            sb.append(p).append(" ");
            sb.append(msg);
            sb.append(CoreStringUtils.fillString(' ', maxMessage - s2));
            sb.append(" ");
        }
        if (maxMessage < s2) {
            maxMessage = s2;
        }
        if (maxMessage > maxColumns) {
            maxMessage = maxColumns;
        }
        return sb.toString();
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

    public float getIndeterminateSize() {
        return indeterminateSize;
    }

    public CProgressBar setIndeterminateSize(float indeterminateSize) {
        if (indeterminateSize <= 0 || indeterminateSize >= 1) {
            indeterminateSize = 0.3f;
        }
        this.indeterminateSize = indeterminateSize;
        return this;
    }
}
