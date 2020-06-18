package net.vpc.app.nuts.runtime.util.console;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.fprint.FPrintCommands;

import java.io.PrintStream;
import java.util.Calendar;

public class CProgressBar {
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
    private long minPeriod = 300;
    private IndeterminatePosition indeterminatePosition = DEFAULT_INDETERMINATE_POSITION;
    private boolean optionNewline;

    public CProgressBar(NutsSession session) {
        this.session = session;
        formatted = session != null;
        if (session != null) {
            optionNewline = NutsWorkspaceUtils.parseProgressOptions(session).contains("newline");
        }
    }

    public CProgressBar(NutsSession ws, int size) {
        this.session = session;
        setSize(size);
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

    private String getStartBracket() {
        if (formatted) {
            return "\\[";
        }
        return "[";
    }

    private String getEndBracket() {
        if (formatted) {
            return "\\]";
        }
        return "]";
    }

    private String getStartFormat() {
        if (formatted) {
            return "##";
        }
        return "";
    }

    private String getEndFormat() {
        if (formatted) {
            return "##";
        }
        return "";
    }

    private String getChar() {
        if (formatted) {
            return "\\*";
        }
        return "*";
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
        this.suffixMoveLineStart=v;
        return this;
    }

    public CProgressBar setPrefixMoveLineStart(boolean v) {
        this.prefixMoveLineStart=v;
        return this;
    }

    public String progress(int percent) {
        long now = System.currentTimeMillis();
        if (now < lastPrint + minPeriod) {
            return "";
        }
        lastPrint = now;
        boolean indeterminate = percent < 0;
        if (indeterminate) {
            StringBuilder formattedLine = new StringBuilder();
            formattedLine.append(getStartBracket());
            formattedLine.append(getStartFormat());
            int indeterminateSize = (int) (this.indeterminateSize * size);
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
                    x = 2 * p - x;
                }
            } else {
                x = 0;
            }

            if (x < 0) {
                x = 0;
            }
            CoreStringUtils.fillString(" ", x, formattedLine);
            CoreStringUtils.fillString(getChar(), indeterminateSize, formattedLine);
            formattedLine.append(getEndFormat());
            int r = size - x - indeterminateSize;
            CoreStringUtils.fillString(' ', r, formattedLine);
            formattedLine.append(getEndBracket());
            return formattedLine.toString();
        } else {
            if (percent > 100) {
                percent = 100 - percent;
            }
            int x = (int) (size / 100.0 * percent);
            StringBuilder formattedLine = new StringBuilder();
            formattedLine.append(getStartBracket());
            if (x > 0) {
                formattedLine.append(getStartFormat());
                CoreStringUtils.fillString(getChar(), x, formattedLine);
                formattedLine.append(getEndFormat());
            }
            CoreStringUtils.fillString(' ', size - x, formattedLine);
            formattedLine.append(getEndBracket());
            return formattedLine.toString();
        }
    }

    public void printProgress(int percent, String msg, PrintStream out) {
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
            s2 = session == null ? msg.length() : session.getWorkspace().io().terminalFormat().textLength(msg);
            if (isPrefixMoveLineStart()) {
                if (optionNewline) {
                    if(!isSuffixMoveLineStart()) {
                        sb.append("\n");
                    }
                } else {
                    sb.append("`" + FPrintCommands.MOVE_LINE_START + "`");
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
                    sb.append("`" + FPrintCommands.LATER_RESET_LINE + "`");
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
