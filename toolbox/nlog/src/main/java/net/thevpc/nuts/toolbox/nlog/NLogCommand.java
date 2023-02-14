/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.toolbox.nlog.filter.AndLineFilter;
import net.thevpc.nuts.toolbox.nlog.model.*;
import net.thevpc.nuts.util.NRef;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author vpc
 */
public class NLogCommand {

    static class Result {
        long count;
        List<PathResult> files = new ArrayList<>();
    }

    static class PathResult {
        Path path;
        List<Line> lines=new ArrayList<>();
        NMsg error;

        public PathResult(Path path) {
            this.path = path;
        }

        public PathResult(Path path, NMsg error) {
            this.path = path;
            this.error = error;
        }

    }

    public boolean run(List<String> paths, NLogFilterConfig config, NSession session, boolean showFileName) {
        boolean plainOut = session.isPlainOut();
        Result result = new Result();
        if (config == null) {
            throw new IllegalArgumentException("missing config");
        }
        if (paths.isEmpty()) {
            throw new IllegalArgumentException("missing path");
        }
        boolean success = true;
        for (String spath : paths) {
            if (spath == null) {
                throw new IllegalArgumentException("missing path");
            }
            Path path = Paths.get(spath);
            if (!Files.exists(path)) {
                NMsg err = NMsg.ofC("path does not exit : %s", path);
                if (plainOut) {
                    session.err().println(err);
                } else {
                    result.files.add(new PathResult(path, err));
                }
                success = false;
            } else if (Files.isRegularFile(path)) {
                if (!runRegularFile(path, config, session, result, showFileName)) {
                    success = false;
                }
            } else if (Files.isDirectory(path)) {
                if (!runDirectory(path, config, session, result, showFileName)) {
                    success = false;
                }
            } else {
                success = false;
                NMsg err = NMsg.ofC("invalid path : %s", path);
                if (plainOut) {
                    session.err().println(err);
                } else {
                    result.files.add(new PathResult(path, err));
                }
            }
        }
        if (!plainOut) {
            session.out().println(result);
        }
        return success;
    }

    public boolean runDirectory(Path path, NLogFilterConfig config, NSession session, Result result, boolean showFileName) {
        boolean success = true;
        try (Stream<Path> list = Files.list(path)) {
            List<Path> all = list.collect(Collectors.toList());
            for (Path p : all) {
                if (Files.isRegularFile(p)) {
                    //should test file names???
                    if (!runRegularFile(path, config, session, result, true)) {
                        success = false;
                    }
                } else if (Files.isDirectory(p)) {
                    if (!runDirectory(p, config, session, result, showFileName)) {
                        success = false;
                    }
                }
            }
        } catch (Exception ex) {
            session.err().println(NMsg.ofC("%s", ex));
            success = false;
        }
        return success;
    }

    public boolean runRegularFile(Path path, NLogFilterConfig config, NSession session, Result result, boolean showFileName) {
        boolean plainOut = session.isPlainOut();
        PathResult pr = new PathResult(path);
        if (showFileName) {
            if (plainOut) {
                session.out().println("");
                session.out().println(NMsg.ofC("-- SCANNING -- %s", path));
            }
        }
        result.files.add(pr);
        long lCount = result.count;
        config = config.copy();
        Integer windowMin = config.getWindowMin();
        if (windowMin == null) {
            config.setWindowMin(windowMin = 0);
        }
        if (config.getWindowMax() == null) {
            config.setWindowMax(0);
        }
        long from = config.getFrom() == null ? 1 : config.getFrom();
        if (from <= 1) {
            from = 1;
        }
        long to = config.getTo() == null ? -1 : config.getTo();
        if (to <= 1) {
            to = -1;
        }
        if (to > 0 && to < from) {
            to = from + 1;
        }
        LineFilter filter = config.getFilter();
        if (filter == null) {
            filter = new AndLineFilter();
        }
        int windowMinBase = Math.max(0, windowMin);
        int hardMin = Math.max(0, Math.max(windowMinBase, filter.getPreviousWindowSize()));
        LineBuffer buffer = new LineBuffer(hardMin + 10);
        int windowMax = config.getWindowMax();
        int windowMaxBase = Math.max(0, windowMax);
        windowMax = Math.max(0, Math.max(windowMaxBase, filter.getNextWindowSize()));

        LineFormat lineFormat = new LineFormat(config);
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            boolean displaySimple = windowMin == 0 && windowMax == 0;
            Line pushedBack = null;
            while (true) {
                Line ll;
                if (pushedBack != null) {
                    ll = pushedBack;
                } else {
                    pushedBack = null;
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    ll = buffer.append(line);
                }
                long rowNum = ll.getNum();
                if (!(rowNum >= from)) {
                    continue;
                }
                if (to > 0) {
                    if (rowNum > to) {
                        break;
                    }
                }
                if (filter.accept(ll)) {
                    lCount++;
                    List<Line> all = new ArrayList<>();
                    int win = windowMinBase + 1 + 1;
                    while (win < windowMin) {
                        Line line2 = buffer.getPrevious(win);
                        if (filter.acceptPrevious(line2, ll, 0, null)) {
                            win++;
                        } else {
                            break;
                        }
                    }
                    all.addAll(buffer.getLast(win + 1));
                    if (!displaySimple) {
                        all.get(all.size() - 1).setMarker(
                                NMsg.ofStyled("[*]", NTextStyle.success())
                        );
                    }
                    int wm = windowMaxBase;
                    while (wm > 0) {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        ll = buffer.append(line);
                        all.add(ll);
                        wm--;
                    }
                    wm = windowMax - windowMaxBase;
                    while (wm > 0) {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        Line ll2 = buffer.append(line);
                        if (filter.acceptNext(ll2, ll, 0, null)) {
                            all.add(ll2);
                            wm--;
                        } else {
                            pushedBack = ll2;
                            break;
                        }
                    }
                    if (plainOut) {
                        if (displaySimple) {
                            displaySimple(all, lineFormat, session, lCount);
                        } else {
                            display(all, lineFormat, session, lCount);
                        }
                    } else {
                        pr.lines.addAll(all);
                    }
                }
            }
            result.count += lCount;
            return true;
        } catch (Exception e) {
            session.err().println(NMsg.ofC("%s is not a valid text file : %s", path, e));
            return false;
        }
    }

    private void display(List<Line> all, LineFormat lineFormat, NSession session, long matchIndex) {
        session.out().println("");
        session.out().println(NMsg.ofC("----%s %s",
                        NMsg.ofStyled("MATCH", NTextStyle.primary1()),
                        matchIndex
                )
        );
        displaySimple(all, lineFormat, session, matchIndex);
    }

    private void displaySimple(List<Line> all, LineFormat lineFormat, NSession session, long matchIndex) {
        for (Line line : all) {
            session.out().println(line.toMsg(lineFormat));
        }
    }

}
