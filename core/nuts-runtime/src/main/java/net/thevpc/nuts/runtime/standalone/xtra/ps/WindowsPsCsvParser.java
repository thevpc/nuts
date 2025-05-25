package net.thevpc.nuts.runtime.standalone.xtra.ps;

import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.io.NpsStatus;
import net.thevpc.nuts.io.NpsType;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.util.NStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

public class WindowsPsCsvParser {
    public NStream<NPsInfo> parse(Reader reader) {
        BufferedReader br = new BufferedReader(reader);
        return NStream.ofIterator(new Iterator<NPsInfo>() {
            NPsInfo last = null;
            boolean headerRead;
            String[] columns;

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
                        last = null;
                        return false;
                    }
                    if (!headerRead) {
                        columns = readColumns(line);
                        headerRead = true;
                        try {
                            line = br.readLine();
                        } catch (IOException e) {
                            throw new NIOException(e);
                        }
                        if (line == null) {
                            return false;
                        }
                    }
                    String[] value = readColumns(line);
                    last = createNPsInfo(value, columns);
                    if (last != null) {
                        return true;
                    }
                }
            }

            @Override
            public NPsInfo next() {
                return last;
            }
        });
    }

    private String[] readColumns(String line) {
        if (line.isEmpty()) return new String[0];
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"'); // double quote inside quoted string
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }


    private NPsInfo createNPsInfo(String[] values, String[] columns) {
        DefaultNPsInfoBuilder v = new DefaultNPsInfoBuilder();
        boolean empty = true;
        for (int i = 0; i < columns.length; i++) {
            String key = columns[i];
            String value = values[i];
            switch (key) {
                case "VSZ": {
                    v.setVirtualMemorySize(NLiteral.of(value).asLong().orElse(0L));
                    empty = false;
                    break;
                }
                case "RSS": {
                    v.setResidentSetSize(NLiteral.of(value).asLong().orElse(0L));
                    empty = false;
                    break;
                }
                case "PID": {
                    v.setId(value);
                    empty = false;
                    break;
                }
                case "TIME": {
                    String normalized = value.replace(',', '.');
                    BigDecimal seconds = new BigDecimal(normalized);
                    long millis = seconds.multiply(BigDecimal.valueOf(1000)).longValueExact();
                    v.setTime(millis);
                    empty = false;
                    break;
                }
                case "STAT": {
                    switch (NStringUtils.trim(value).toLowerCase()) {
                        case "suspended": {
                            v.setStatus(NpsStatus.SUSPENDED);
                            break;
                        }
                        case "sleeping": {
                            v.setStatus(NpsStatus.WAITING_FOR_EVENT);
                            break;
                        }
                        case "running": {
                            v.setStatus(NpsStatus.RUNNING);
                            break;
                        }
                        case "idle": {
                            v.setStatus(NpsStatus.IDLE);
                            break;
                        }
                        default: {
                            v.setStatus(NpsStatus.UNKNOWN);
                            break;
                        }
                    }
                    empty = false;
                    break;
                }
                case "MEM": {
                    v.setPercentMem(NLiteral.of(value.replace(",", ".")).asDouble().orElse(0.0));
                    empty = false;
                    break;
                }
                case "USER": {
                    v.setUser(value);
                    empty = false;
                    break;
                }
                case "TTY": {
                    if ("N/A".equals(value)) {
                        //ignore
                    }
                    //
                    empty = false;
                    break;
                }
                case "START": {
                    if ("N/A".equals(value)) {
                        //ignore
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

                        // Parse the string into a LocalDateTime
                        LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);

                        // Convert LocalDateTime to Instant (Assuming UTC for simplicity)
                        v.setStartTime(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    }
                    empty = false;
                    break;
                }
                case "COMMAND": {
                    setCommand(v, value);
                    empty = false;
                    break;
                }
            }
        }
        if (empty) {
            return null;
        }
        return _build(v);
    }

    private NPsInfo _build(DefaultNPsInfoBuilder v) {
        v.setType(NpsType.PROCESS);
        return v.build();
    }

    private void setCommand(DefaultNPsInfoBuilder v, String line) {
        v.setCmdLine(line);
        NCmdLines nCmdLines = NCmdLines.of().setShellFamily(NShellFamily.WIN_CMD).setLenient(true);
        try {
            v.setCmdLineArgs(nCmdLines.parseCmdLine(line).map(NCmdLine::toStringArray).orElse(null));
        } catch (Exception ex) {
            if (line.indexOf("\"\"") >= 0) {
                line = line.replace("\"\"", "\"");
                try {
                    v.setCmdLineArgs(nCmdLines.parseCmdLine(line).map(NCmdLine::toStringArray).orElse(null));
                } catch (Exception ex2) {
                    //System.err.println("really??");
                }
            }
        }
        if (v.getCmdLineArgs() != null && v.getCmdLineArgs().length > 0) {
            String[] a = v.getCmdLineArgs();
            if (a.length > 0) {
                int x = Math.max(a[0].lastIndexOf("/"), a[0].lastIndexOf("\\"));
                if (x >= 0) {
                    v.setName(NStringUtils.trimToNull(a[0].substring(x + 1)));
                } else {
                    v.setName(NStringUtils.trimToNull(a[0]));
                }
            }
        } else {
            int x = Math.max(v.getCmdLine().lastIndexOf("/"), v.getCmdLine().lastIndexOf("\\"));
            if (x >= 0) {
                v.setName(NStringUtils.trimToNull(v.getCmdLine().substring(x + 1)));
            } else {
                v.setName(NStringUtils.trimToNull(v.getCmdLine()));
            }
        }
    }
}
