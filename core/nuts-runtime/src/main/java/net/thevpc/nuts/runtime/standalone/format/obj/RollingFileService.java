package net.thevpc.nuts.runtime.standalone.format.obj;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RollingFileService {
    public static final String YYYY_M_MDD_H_HMMSS_SSS = "yyyyMMddHHmmssSSSSSS";
    private DecimalFormat doubleFormat;
    private String ps;
    private Pattern p;
    private NPath folder;
    private String fileName;
    private String filePatternSimple;
    private int count;


    public RollingFileService(NPath folder, int count, NSession session) {
        this.count = count;
        int cc = String.valueOf(count).length();
        StringBuilder df = new StringBuilder();
        for (int i = 0; i < cc; i++) {
            df.append('0');
        }
        doubleFormat = new DecimalFormat(df.toString());
        this.folder = folder.toAbsolute().getParent();
        if (this.folder == null) {
            this.folder = NPath.ofUserDirectory();
        }
        this.fileName = folder.getName();
        if (this.fileName.indexOf('#') < 0) {
            String b = folder.getNameParts().getBaseName();
            String e = folder.getNameParts().getExtension();
            this.fileName = b + "#" + e;
        }
        char[] t = fileName.toCharArray();
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        boolean visited = false;
        for (int j = 0; j < t.length; j++) {
            char c = t[j];
            if (c == '#' || c == '*') {
                if (visited) {
                    throw new NoSuchElementException("already found */#");
                }
                visited = true;
                if (j > 0) {
                    char pp = t[j - 1];
                    if (
                            Character.isAlphabetic(pp) ||
                                    Character.isLetterOrDigit(pp)
                    ) {
                        sb1.append('-');
                        sb2.append('-');
                    }
                    sb1.append("(?<t>\\d{" + (YYYY_M_MDD_H_HMMSS_SSS.length()) + "})-(?<n>\\d{1,10})");
                    sb2.append('#');
                }
                if (j < t.length - 1) {
                    char pp = t[j + 1];
                    if (
                            Character.isAlphabetic(pp) ||
                                    Character.isLetterOrDigit(pp)
                    ) {
                        sb1.append('-');
                        sb2.append('-');
                    }
                }
            } else {
                switch (c) {
                    case '\\':
                    case '/':
                    case ':':
                    case '<':
                    case '>':
                    case '[':
                    case ']':
                    case '(':
                    case ')':
                    case '?':
                    case '^':
                    case '$': {
                        throw new NIllegalArgumentException(NMsg.ofC("unsupported %s", c));
                    }
                    case '.': {
                        sb1.append("[.]");
                        sb2.append(".");
                        break;
                    }
                    default: {
                        sb1.append(c);
                        sb2.append(c);
                    }
                }
            }
        }
        String z = sb1.toString().trim();
        if (z.isEmpty()) {
            z = "(?<t>\\d{17})-(?<n>\\d{1,10})";
        }
        ps = z;

        z = sb2.toString().trim();
        if (z.isEmpty()) {
            z = "#";
        }
        filePatternSimple = z;

        p = Pattern.compile(ps);
    }

    public NPath roll() {
        List<PathAndNbr> r = Arrays.stream(load()).map(x -> toPathAndNbr(x).get()).collect(Collectors.toList());
        int max = Math.min(count, r.size());
        for (int i = r.size() - 1; i >= max; i--) {
            PathAndNbr u = r.get(i);
            u.p.delete();
        }
        for (int i = max - 1; i >= 0; i--) {
            PathAndNbr u = r.get(i);
            if (i == count - 1) {
                u.p.delete();
            } else {
                rename(u, i + 2);
            }
        }
        return folder.resolve(nextFileName(Instant.now(), 1));
    }

    private String nextFileName(Instant instant, int number) {
        return filePatternSimple.replace("#",
                new SimpleDateFormat(YYYY_M_MDD_H_HMMSS_SSS).format(
                        Date.from(instant)
                )
                        + "-"
                        + doubleFormat.format(number)
        );
    }

    private void rename(PathAndNbr nPath, int i) {
        nPath.p.moveTo(
                folder.resolve(nextFileName(nPath.time, i)),
                NPathOption.REPLACE_EXISTING
        );
    }

    private NOptional<PathAndNbr> toPathAndNbr(NPath x) {
        Matcher matcher = p.matcher(x.getName());
        if (matcher.matches()) {
            String n = matcher.group("n");
            String t = matcher.group("t");
            Date localDateTime = null;
            try {
                localDateTime = new SimpleDateFormat(YYYY_M_MDD_H_HMMSS_SSS).parse(t);
            } catch (ParseException e) {
                return NOptional.ofEmpty();
            }
            Instant instant = localDateTime.toInstant();
            return NOptional.of(new PathAndNbr(x,
                    instant,
                    new BigInteger(n)));
        }
        return NOptional.ofEmpty();
    }

    private NPath[] load() {
        return folder.list().stream()
                .map(x -> toPathAndNbr(x).orNull()).filter(x -> x != null)
                .sorted()
                .map(x -> x.p).toArray(NPath[]::new);
    }

}
