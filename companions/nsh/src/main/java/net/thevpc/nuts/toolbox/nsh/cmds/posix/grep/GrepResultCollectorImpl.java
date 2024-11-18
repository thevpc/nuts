package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

import net.thevpc.nuts.util.NAsk;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.toolbox.nsh.cmds.util.NNumberedObject;
import net.thevpc.nuts.toolbox.nsh.util.ColumnRuler;
import net.thevpc.nuts.toolbox.nsh.util.FileInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class GrepResultCollectorImpl implements GrepResultCollector {
    List<GrepResultItem> results = new ArrayList<>();
    NSession session;
    GrepOptions options;
    NPrintStream out;
    boolean prefixFileName;
    int max;
    boolean first = true;
    boolean stopped = false;
    long linesCount;
    long matchCount;
    long filesCount;

    public GrepResultCollectorImpl(NSession session, GrepOptions options, NPrintStream out, boolean prefixFileName, int max) {
        this.session = session;
        this.options = options;
        this.out = out;
        this.prefixFileName = prefixFileName;
        this.max = max;
    }

    @Override
    public boolean acceptMatch(GrepResultItem grepResultItem) {
        if (stopped) {
            return false;
        }
        if (grepResultItem.match) {
            matchCount++;
        }
        linesCount++;
        results.add(grepResultItem);
        if (results.size() >= max) {
            if (!flush()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void acceptFile(FileInfo f) {
        filesCount++;
    }

    @Override
    public void acceptLine(NNumberedObject<String> line) {
        linesCount++;
    }

    @Override
    public long getMatchCount() {
        return matchCount;
    }

    public long getLinesCount() {
        return linesCount;
    }

    @Override
    public long getFilesCount() {
        return filesCount;
    }

    public boolean flush() {
        if (results.isEmpty()) {
            return true;
        }
        switch (session.getOutputFormat().orDefault()) {
            case PLAIN: {
                ColumnRuler ruler = new ColumnRuler();
                for (GrepResultItem result : results) {
                    if (result.match) {
                        if (first) {
                            first = false;
                        } else {
                            if (options.byLine) {
                                String v = NAsk.of()
                                        .forString(NMsg.ofPlain("continue"))
                                        .setDefaultValue("y")
                                        .getValue();
                                if (!NLiteral.of(v).asBoolean().orElse(false)) {
                                    stopped = true;
                                    return false;
                                }
                            }
                        }
                    }
                    if (options.n) {
                        if (result.path != null && prefixFileName) {
                            out.print(result.path);
                            out.print(":");
                        }
                        out.print(ruler.nextNum(result.number, session));
                    }
                    out.println(result.line);
                }
                break;
            }
            default: {
                if (options.n) {
                    out.println(results);
                } else {
                    out.println(results.stream().map(r -> r.line).collect(Collectors.toList()));
                }
            }
        }
        results.clear();
        return true;
    }
}
