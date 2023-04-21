package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyles;

import java.util.ArrayList;
import java.util.List;

public class MultiGrepFilter implements GrepFilter {
    public List<GrepFilter> positive = new ArrayList<>();
    public List<GrepFilter> negative = new ArrayList<>();

    public MultiGrepFilter(List<ExpressionInfo> a, boolean lineRegexp) {
        for (ExpressionInfo e : a) {
            if (e.isInvertMatch()) {
                negative.add(
                        new PatternGrepFilter(e.getPattern(), e.isWord(), lineRegexp, e.isIgnoreCase())
                );
            } else {
                positive.add(
                        new PatternGrepFilter(e.getPattern(), e.isWord(), lineRegexp, e.isIgnoreCase())
                );
            }
        }
        if (negative.isEmpty() && positive.isEmpty()) {
            positive.add(new NoGrepFilter());
        }
    }

    public void processNonPivot(String line, NTextBuilder coloredLine, NTextStyles selectionStyle, NSession session) {
        if (positive.size() > 0) {
            for (GrepFilter g : positive) {
                g.processNonPivot(line, coloredLine, selectionStyle, session);
            }
        }
    }

    @Override
    public boolean processPivot(String line, NTextBuilder coloredLine, NTextStyles selectionStyle, NSession session) {
        if (negative.size() > 0) {
            for (GrepFilter g : negative) {
                if (g.processPivot(line, coloredLine, selectionStyle, session)) {
                    return false;
                }
            }
        }
        if (positive.size() > 0) {
            for (GrepFilter g : positive) {
                if (g.processPivot(line, coloredLine, selectionStyle, session)) {
                    return true;
                }
            }
        }
        return false;
    }


}
