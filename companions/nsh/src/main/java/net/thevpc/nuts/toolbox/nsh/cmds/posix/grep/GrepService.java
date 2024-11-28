package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.toolbox.nsh.cmds.util.BufferedLineIterator;
import net.thevpc.nuts.toolbox.nsh.cmds.util.NNumberedObject;
import net.thevpc.nuts.toolbox.nsh.cmds.util.WindowFilterIterator;
import net.thevpc.nuts.toolbox.nsh.cmds.util.WindowObject;
import net.thevpc.nuts.toolbox.nsh.util.FileInfo;
import net.thevpc.nuts.util.NGlob;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class GrepService {
    public void run(GrepOptions options, NSession session){
        NPrintStream out = session.out();
        if (options.files.isEmpty()) {
            options.files.add(null);
        }
        GrepFilter p = new MultiGrepFilter(options.expressions, options.lineRegexp);

        //text mode
        boolean prefixFileName = false;
        if ((options.files.size() > 1) || (
                options.files.size() == 1
                        && options.files.get(0).getPath() != null
                        && options.files.get(0).getPath().isDirectory())) {
            prefixFileName = true;
        }
        GrepResultCollectorImpl grepResultCollectorImpl = new GrepResultCollectorImpl(session, options, out, prefixFileName, 1024);
        Predicate<NPath> fileName = new Predicate<NPath>() {
            List<Pattern> patterns = new ArrayList<>();

            {
                for (String fileName : options.fileNames) {
                    patterns.add(NGlob.of().toPattern(fileName));
                }
                for (String fileName : options.fileNames) {
                    patterns.add(
                            Pattern.compile(NGlob.of().toPatternString(fileName))
                    );
                }
            }

            @Override
            public boolean test(NPath nPath) {
                if (patterns.isEmpty()) {
                    return true;
                }
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(nPath.getName()).matches()) {
                        return true;
                    }
                }
                return false;
            }
        };
        for (FileInfo f : options.files) {
            if (f.getPath() == null) {
                grepFile(f, p, options, session, prefixFileName, grepResultCollectorImpl);
            } else if (f.getPath().isRegularFile()) {
                grepFile(f, p, options, session, prefixFileName, grepResultCollectorImpl);
            } else if (f.getPath().isDirectory()) {
                if (options.recursive) {
                    Stack<FileInfo> stack = new Stack<>();
                    stack.add(f);
                    while (!stack.isEmpty()) {
                        FileInfo ff = stack.pop();
                        if (ff.getPath().isRegularFile()) {

                            grepFile(f, p, options, session, prefixFileName, grepResultCollectorImpl);
                        } else if (ff.getPath().isDirectory()) {
                            for (NPath nPath : ff.getPath().list()) {
                                if (nPath.isDirectory() || (nPath.isRegularFile() && fileName.test(nPath))) {
                                    stack.push(
                                            new FileInfo(
                                                    nPath, f.getHighlighter()
                                            )
                                    );
                                }
                            }
                        }
                    }
                }
            } else {

            }
        }
        grepResultCollectorImpl.flush();
        if (options.summary) {
            session.out().println(
                    NMaps.of(
                            "files", grepResultCollectorImpl.getFilesCount(),
                            "match", grepResultCollectorImpl.getMatchCount(),
                            "lines", grepResultCollectorImpl.getLinesCount()
                    )
            );
        }
    }


    protected boolean grepFile(FileInfo f, GrepFilter p, GrepOptions options, NSession session, boolean prefixFileName, GrepResultCollector results) {

        Reader reader = null;
        boolean closeReader = false;
        long count = 0;
        try {
            if (f == null) {
                return processByLine(options, p, f, results, session);
            } else if (f.getPath().isDirectory()) {
                for (NPath ff : f.getPath().stream()) {
                    if (!grepFile(new FileInfo(ff, f.getHighlighter()), p, options, session, true, results)) {
                        return false;
                    }
                }
                return true;
            } else {
                if (f.getHighlighter() == null) {
                    return processByLine(options, p, f, results, session);
                } else {
                    return processByText(options, p, f, results, session);
                }
            }
        } catch (IOException ex) {
            throw new NExecutionException(NMsg.ofC("%s", ex), ex, NExecutionException.ERROR_3);
        }
    }

    private boolean isNewLine(NText t) {
        if (t.getType() == NTextType.PLAIN) {
            String txt = ((NTextPlain) t).getText();
            return (txt.equals("\n") || txt.equals("\r\n"));
        }
        return false;
    }

    private NTextBuilder readLine(NTextBuilder flattened, NSession session) {
        if (flattened.size() == 0) {
            return null;
        }
        List<NText> r = new ArrayList<>();
        while (flattened.size() > 0) {
            NText t = flattened.get(0);
            flattened.removeAt(0);
            if (isNewLine(t)) {
                break;
            }
            r.add(t);
        }
        return NTextBuilder.of().appendAll(r);
    }

    private boolean processByLine(GrepOptions options, GrepFilter p, FileInfo f, GrepResultCollector results, NSession session) throws IOException {
        try (Reader reader = (f == null ? new InputStreamReader(session.in()) : f.getPath().getReader())) {
            return processByText0(reader, null, options, p, f, results, session);
        }
    }

    private List<GrepResultItem> createResult(WindowObject<NNumberedObject<String>> wline, NTextBuilder coloredLine, GrepOptions options, GrepFilter filter, FileInfo f, GrepResultCollector results, NSession session) {
        List<GrepResultItem> result = new LinkedList<>();
        List<NNumberedObject<String>> items = wline.getItems();
        for (int i = 0; i < items.size(); i++) {
            NNumberedObject<String> line = items.get(i);
            //long nn, String line
            NTextBuilder coloredLine0 = coloredLine;
            if (coloredLine0 == null) {
                coloredLine0 = NText.ofCode(f.getHighlighter(), line.getObject()).highlight().builder();
            }
            results.acceptLine(line);
            if (i == wline.getPivotIndex()) {
                if (filter.processPivot(line.getObject(), coloredLine0, selectionStyle(options), session)) {
                    result.add(new GrepResultItem(f.getPath(), line.getNumber(), coloredLine0.build(), true));
                }
            } else {
                result.add(new GrepResultItem(f.getPath(), line.getNumber(), coloredLine0.build(), false));
            }
        }
        return result;
    }

    private boolean processByText(GrepOptions options, GrepFilter p, FileInfo f, GrepResultCollector results, NSession session) throws IOException {
        String text = new String(NCp.of().from(f.getPath()).getByteArrayResult());
        if (NBlankable.isBlank(f.getHighlighter())) {
            f.setHighlighter(f.getPath().getContentType());
        }
        NTextBuilder flattened = NText.ofCode(f.getHighlighter(), text).highlight()
                .builder()
                .flatten();
        try (Reader in = f.getPath().getReader()) {
            return processByText0(in, flattened, options, p, f, results, session);
        }
    }

    private boolean processByText0(Reader reader, NTextBuilder flattened, GrepOptions options, GrepFilter p, FileInfo f, GrepResultCollector results, NSession session) {
        Iterator<NNumberedObject<String>> li = new BufferedLineIterator(reader, options.from, options.to);
        Iterator<WindowObject<NNumberedObject<String>>> it;
        results.acceptFile(f);
        if (options.windowFilter.isEmpty()) {
            it = new Iterator<WindowObject<NNumberedObject<String>>>() {
                @Override
                public boolean hasNext() {
                    return li.hasNext();
                }

                @Override
                public WindowObject<NNumberedObject<String>> next() {
                    NNumberedObject<String> next = li.next();
                    return next == null ? null : new WindowObject<>(Arrays.asList(next), 0);
                }
            };
        } else {
            it = new WindowFilterIterator<>(li, options.windowFilter.build(), 0, 0);
        }

        while (it.hasNext()) {
            WindowObject<NNumberedObject<String>> line = it.next();
            NTextBuilder coloredLine = flattened == null ? null : readLine(flattened, session);
            for (GrepResultItem grepResultItem : createResult(line, coloredLine, options, p, f, results, session)) {
                if (!results.acceptMatch(grepResultItem)) {
                    return false;
                }
            }
        }
        return true;
    }


    public NTextStyles selectionStyle(GrepOptions options) {
        String s = options.selectionStyle;
        NTextStyles def = NTextStyles.of(NTextStyle.secondary(2));
        if (NBlankable.isBlank(s)) {
            return def;
        }
        return NTextStyles.parse(s).orElse(def);
    }

}
