package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyles;

public interface GrepFilter {

    void processNonPivot(String line, NTextBuilder coloredLine, NTextStyles selectionStyle, NSession session);

    boolean processPivot(String line, NTextBuilder coloredLine, NTextStyles selectionStyle, NSession session);
}
