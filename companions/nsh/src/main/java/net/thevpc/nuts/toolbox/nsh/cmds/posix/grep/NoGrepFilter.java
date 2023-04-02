package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NTexts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoGrepFilter implements GrepFilter {
    public NoGrepFilter() {
    }

    public void processNonPivot(String line, NTextBuilder coloredLine, NTextStyles selectionStyle, NSession session) {

    }
        @Override
    public boolean processPivot(String line, NTextBuilder coloredLine, NTextStyles selectionStyle, NSession session) {
        return true;
    }
}
