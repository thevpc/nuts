package net.thevpc.nuts.runtime.standalone.xtra.ps;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.io.NpsStatus;
import net.thevpc.nuts.io.NpsType;
import net.thevpc.nuts.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public class UnixPsParser {

    public NStream<NPsInfo> parse(Reader reader) {
        BufferedReader br = new BufferedReader(reader);
        try {
            br.readLine();
        } catch (IOException e) {
            throw new NIOException(e);
        }
        CharPredicate spaces = c -> c == ' ' || c == '\t';
        return NStream.ofIterator(new Iterator<NPsInfo>() {
            NPsInfo last = null;

            @Override
            public boolean hasNext() {
                while (true) {
                    String line = null;
                    try {
                        line = br.readLine();
                    } catch (IOException e) {
                        throw new NIOException(e);
                    }
                    if (line == null) {
                        return false;
                    }
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    NStringBuilder sb = new NStringBuilder(line);
                    DefaultNPsInfoBuilder pi = new DefaultNPsInfoBuilder();
                    pi.setUser(sb.readUntil(spaces));
                    sb.readWhile(spaces);

                    pi.setId(sb.readUntil(spaces));
                    sb.readWhile(spaces);

                    pi.setPercentCpu(NLiteral.of(sb.readUntil(spaces)).asDouble().orElse(0.0));
                    sb.readWhile(spaces);

                    pi.setPercentMem(NLiteral.of(sb.readUntil(spaces)).asDouble().orElse(0.0));
                    sb.readWhile(spaces);

                    pi.setVirtualMemorySize(NLiteral.of(sb.readUntil(spaces)).asLong().orElse(0L));
                    sb.readWhile(spaces);

                    pi.setResidentSetSize(NLiteral.of(sb.readUntil(spaces)).asLong().orElse(0L));
                    sb.readWhile(spaces);


                    pi.setTerminal(parseTty(sb.readUntil(spaces)));
                    sb.readWhile(spaces);

                    String stat = sb.readUntil(spaces);
                    sb.readWhile(spaces);
                    Set<String> s = new java.util.HashSet<>();
                    pi.setStatus(parseStat(stat, s));
                    pi.setStatusFlags(s);

                    pi.setStartTime(parseStartDateAux(sb.readUntil(spaces)));
                    sb.readWhile(spaces);

                    pi.setTime(parseTime(sb.readUntil(spaces)));
                    sb.readWhile(spaces);

                    String cmd = sb.readUntil(spaces);
                    sb.readWhile(spaces);
                    if (cmd == null) {
                        pi.setType(NpsType.UNKNOWN);
                        pi.setCmdLine(cmd);
                        pi.setCmdLineArgs(null);
                    } else if (cmd.startsWith("[") && cmd.endsWith("]")) {
                        pi.setType(NpsType.KERNEL_THREAD);
                        cmd = cmd.substring(1, cmd.length() - 1);
                        pi.setCmdLine(cmd);
                        pi.setCmdLineArgs(null);
                        int x = cmd.indexOf("/");
                        if (x >= 0) {
                            pi.setName(cmd.substring(0, x));
                        } else {
                            pi.setName(cmd);
                        }
                    } else {
                        pi.setType(NpsType.PROCESS);
                        pi.setCmdLine(cmd);
                        pi.setCmdLineArgs(NCmdLine.parse(cmd).map(NCmdLine::toStringArray).orElse(null));
                        String[] a = pi.getCmdLineArgs();
                        if (a.length > 0) {
                            int x = a[0].lastIndexOf("/");
                            if (x >= 0) {
                                pi.setName(a[0].substring(x + 1));
                            } else {
                                pi.setName(cmd);
                            }
                        }
                    }
                    last = pi.build();
                    return true;
                }
            }

            @Override
            public NPsInfo next() {
                return last;
            }
        });
    }

    public static long parseTime(String time) {
        String[] parts = time.split(":");
        try {
            if (parts.length == 2) {
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return ((minutes * 60 + seconds) * 1000);
            } else if (parts.length == 3) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);
                return ((hours * 3600 + minutes * 60 + seconds) * 1000);
            } else {
                //
            }
        } catch (NumberFormatException e) {
            //
        }
        return 0;
    }

    public static NpsStatus parseStat(String stat, Set<String> s) {
        NpsStatus status = null;
        if (stat != null) {
            for (char c : stat.toCharArray()) {
                switch (c) {
//                                D    uninterruptible sleep (usually I/O)
                    case 'D': {
                        status = (NpsStatus.WAITING_FOR_IO);
                        break;
                    }
//                                I    idle kernel thread
                    case 'I': {
                        status = (NpsStatus.IDLE);
                        break;
                    }
//                                R    running or runnable (on run queue)
                    case 'R': {
                        status = (NpsStatus.RUNNING);
                        break;
                    }
//                                S    interruptible sleep (waiting for an event to complete)
                    case 'S': {
                        status = (NpsStatus.WAITING_FOR_EVENT);
                        break;
                    }
//                                T    stopped by job control signal
                    case 'T': {
                        status = (NpsStatus.STOPPED);
                        break;
                    }
//                                t    stopped by debugger during the tracing
                    case 't': {
                        status = (NpsStatus.STOPPED);
                        break;
                    }
//                                W    paging (not valid since Linux 2.6)
                    case 'W': {
                        status = (NpsStatus.WAITING_FOR_IO);
                        break;
                    }
//                                X    dead (should never be seen)
                    case 'X': {
                        status = (NpsStatus.DEAD);
                        break;
                    }
//                                Z    defunct (“zombie”) process, terminated but not reaped by its parent
                    case 'Z': {
                        status = (NpsStatus.ZOMBIE);
                        break;
                    }

//                                <    high-priority (not nice to other users)
                    case '<': {
                        s.add("high-priority");
                        break;
                    }
//                                N    low-priority (nice to other users)
                    case 'N': {
                        s.add("low-priority");
                        break;
                    }
//                                L    has pages locked into memory (for real-time and custom I/O)
                    case 'L': {
                        s.add("pages-locked");
                        break;
                    }
//                                s    is a session leader
                    case 's': {
                        s.add("session-leader");
                        break;
                    }
//                                l    is multi-threaded (using CLONE_THREAD, like NPTL pthreads do)
                    case 'l': {
                        s.add("multi-threaded");
                        break;
                    }
//                                +    is in the foreground process group
                    case '+': {
                        s.add("foreground");
                        break;
                    }
                    default: {
                        s.add("unknown-" + String.valueOf(c));
                    }
                }
            }
        }
        return status;
    }

    public static String parseTty(String tty) {
        if (tty == null) {
            return null;
        }
        tty = tty.trim();
        if (tty.isEmpty()) {
            return null;
        }
        if (tty.equals("?")) {
            return null;
        }
        return tty;
    }

    public static Instant parseStartDateAux(String auxStatDate) {
        if (NBlankable.isBlank(auxStatDate)) {
            return null;
        }
        if (auxStatDate.length() == 5 && auxStatDate.matches("[a-zA-Z]{3}[0-9]{2}")) {
            int year = Integer.parseInt(auxStatDate.substring(auxStatDate.length() - 2));
            String monthName = auxStatDate.substring(0, auxStatDate.length() - 2);
            int month = 1;
            switch (monthName.trim().toLowerCase()) {
                case "jan": {
                    month = 1;
                    break;
                }
                case "feb": {
                    month = 2;
                    break;
                }
                case "mar": {
                    month = 3;
                    break;
                }
                case "apr": {
                    month = 4;
                    break;
                }
                case "may": {
                    month = 5;
                    break;
                }
                case "jun": {
                    month = 6;
                    break;
                }
                case "jul": {
                    month = 7;
                    break;
                }
                case "aug": {
                    month = 8;
                    break;
                }
                case "sep": {
                    month = 9;
                    break;
                }
                case "oct": {
                    month = 10;
                    break;
                }
                case "nov": {
                    month = 11;
                    break;
                }
                case "dec": {
                    month = 12;
                    break;
                }
                default: {
                    return null;
                }
            }
            String yearString = String.format("%04d", 2000 + year);
            String monthString = String.format("%02d", month);
            String fullDateStr = yearString + "-" + monthString + "-01";
            LocalDate localDate = LocalDate.parse(fullDateStr); // uses ISO_LOCAL_DATE format
            LocalDateTime dateTime = localDate.atStartOfDay(); // midnight
            return dateTime.atZone(ZoneId.systemDefault()).toInstant();
        } else if (auxStatDate.matches("[0-9]+:[0-9]+")) {
            String[] parts = auxStatDate.split(":");
            String timeStr = String.format("%02d", NLiteral.of(parts[0]).asInt().get())
                    + ":" + String.format("%02d", NLiteral.of(parts[1]).asInt().get())
                    + ":00";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime time = LocalTime.parse(timeStr, formatter);
            LocalDateTime dateTime = LocalDate.now().atTime(time);
            return dateTime.atZone(ZoneId.systemDefault()).toInstant();
        } else if (auxStatDate.matches("[0-9]{4}")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
            LocalTime time = LocalTime.parse(auxStatDate, formatter);
            LocalDateTime dateTime = LocalDate.now().atTime(time);
            return dateTime.atZone(ZoneId.systemDefault()).toInstant();
        } else {
            return null;
        }
    }

    public static Instant parseStartDateLong(String lstart) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH);
        LocalDateTime dateTime = LocalDateTime.parse(lstart.replace("  "," "), formatter);
        return dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
