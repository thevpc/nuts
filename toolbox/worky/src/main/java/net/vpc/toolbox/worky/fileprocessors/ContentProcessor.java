package net.vpc.toolbox.worky.fileprocessors;

import net.vpc.common.textsource.JTextSource;
import net.vpc.common.textsource.log.JTextSourceLog;

import java.nio.file.Path;

public interface ContentProcessor {
    String processSource(JTextSource source, JTextSourceLog messages);

    void processRegularFile(Path path, JTextSourceLog messages);
}
