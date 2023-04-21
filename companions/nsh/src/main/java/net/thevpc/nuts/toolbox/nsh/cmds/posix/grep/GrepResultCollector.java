package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

import net.thevpc.nuts.toolbox.nsh.cmds.util.NNumberedObject;
import net.thevpc.nuts.toolbox.nsh.util.FileInfo;

public interface GrepResultCollector {
    boolean acceptMatch(GrepResultItem item);
    long getLinesCount();
    long getMatchCount();
    long getFilesCount();

    void acceptFile(FileInfo f);

    void acceptLine(NNumberedObject<String> line);
}
