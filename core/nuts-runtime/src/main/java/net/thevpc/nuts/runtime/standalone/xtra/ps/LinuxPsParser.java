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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public class LinuxPsParser {
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

                    pi.setTerminal(UnixPsParser.parseTty(sb.readUntil(spaces)));
                    sb.readWhile(spaces);

                    String stat = sb.readUntil(spaces);
                    sb.readWhile(spaces);
                    Set<String> s = new java.util.HashSet<>();
                    pi.setStatus(UnixPsParser.parseStat(stat, s));
                    pi.setStatusFlags(s);

                    String lstart = sb.readCount("Tue Apr 22 18:03:39 2025".length());
                    sb.readWhile(spaces);
                    pi.setStartTime(UnixPsParser.parseStartDateLong(lstart));

                    pi.setTime(UnixPsParser.parseTime(sb.readUntil(spaces)));
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
                            pi.setName(NStringUtils.trimToNull(cmd.substring(0, x)));
                        } else {
                            pi.setName(NStringUtils.trimToNull(cmd));
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
}
