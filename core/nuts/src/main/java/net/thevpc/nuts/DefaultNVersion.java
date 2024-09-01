/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNVersion implements NVersion {
    private static Pattern VERSION_PART_PATTERN = Pattern.compile("([0-9a-zA-Z_.-])+");

    private static final long serialVersionUID = 1L;
    protected String expression;
    private VersionParts parts;

    public DefaultNVersion(String expression) {
        this.expression = (NStringUtils.trim(expression));
    }

    public static String incVersion(String oldVersion, int level, long count) {
        return incVersion(oldVersion, level, BigInteger.valueOf(count));
    }

    public static String incVersion(String oldVersion, int level, BigInteger count) {
        if (count == null) {
            count = BigInteger.ZERO;
        }
        VersionParts parts = splitVersionParts2(oldVersion);
        int digitCount = parts.getDigitCount();
        if (digitCount == 0) {
            parts.addDigit(BigInteger.ZERO, ".");
            digitCount = parts.getDigitCount();
        }
        if (level < 0) {
            level = digitCount + level;
            while (level < 0) {
                parts.addDigit(BigInteger.ZERO, ".");
                level++;
            }
            VersionPart digit = parts.getDigit(level);
            digit.string = String.valueOf(new BigInteger(digit.string).add(count));
            return parts.toString();
        } else {
            for (int i = digitCount; i < level; i++) {
                parts.addDigit(BigInteger.ZERO, ".");
            }
            VersionPart digit = parts.getDigit(level);
            digit.string = String.valueOf(new BigInteger(digit.string).add(count));
            return parts.toString();
        }
    }


    public static int compareVersions(String v1, String v2) {
        v1 = NStringUtils.trim(v1);
        v2 = NStringUtils.trim(v2);
        if (v1.equals(v2)) {
            return 0;
        }
        if (NConstants.Versions.LATEST.equals(v1)) {
            return 1;
        }
        if (NConstants.Versions.LATEST.equals(v2)) {
            return -1;
        }
        if (NConstants.Versions.RELEASE.equals(v1)) {
            return 1;
        }
        if (NConstants.Versions.RELEASE.equals(v2)) {
            return -1;
        }
        VersionParts v1arr = splitVersionParts2(v1);
        VersionParts v2arr = splitVersionParts2(v2);
        return v1arr.compareTo(v2arr);
    }


    private static VersionParts splitVersionParts2(String v1) {
        v1 = NStringUtils.trim(v1);
        List<VersionPart> parts = new ArrayList<>();
        StringBuilder last = null;
        VersionPartType partType = null;
        for (char c : v1.toCharArray()) {
            if (Character.isDigit(c)) {
                if (last == null) {
                    last = new StringBuilder();
                    last.append(c);
                    partType = VersionPartType.NUMBER;
                } else if (partType == VersionPartType.NUMBER) {
                    last.append(c);
                } else {
                    parts.add(new VersionPart(last.toString(), partType));
                    last.delete(0, last.length());
                    partType = VersionPartType.NUMBER;
                    last.append(c);
                }
            } else if (c == '.' || c == '-') {
                if (last != null) {
                    parts.add(new VersionPart(last.toString(), partType));
                    last = null;
                }
                parts.add(new VersionPart(String.valueOf(c), VersionPartType.SEPARATOR));
                partType = VersionPartType.SEPARATOR;
            } else {
                if (last == null) {
                    partType = VersionPartType.QAL;
                    last = new StringBuilder();
                    last.append(c);
                } else if (partType == VersionPartType.QAL) {
                    last.append(c);
                } else {
                    parts.add(new VersionPart(last.toString(), partType));
                    partType = VersionPartType.QAL;
                    last.delete(0, last.length());
                    last.append(c);
                }
            }
        }
        if (last != null && last.length() > 0) {
            parts.add(new VersionPart(last.toString(), partType));
        }
        return new VersionParts(parts);
    }


    private static Integer getKnownQualifierIndex(String v1) {
        switch (v1.toLowerCase()) {
            case "a":
            case "alpha":
                return 1;
            case "b":
            case "beta":
                return 2;
            case "m":
            case "milestone":
                return 3;
            case "rc":
            case "cr":
                return 4;
            case "snapshot":
                return 5;
            case "":
            case "ga":
            case "final":
                return 6;
            case "sp":
                return 7;
        }
        return null;
    }

    public boolean isLatestVersion() {
        String s = asSingleValue().orNull();
        return NConstants.Versions.LATEST.equalsIgnoreCase(s);
    }

    public boolean isReleaseVersion() {
        String s = asSingleValue().orNull();
        return NConstants.Versions.RELEASE.equalsIgnoreCase(s);
    }

    @Override
    public boolean isSnapshotVersion() {
        String s = asSingleValue().orNull();
        return s != null && s.toUpperCase().endsWith("-SNAPSHOT");
    }

    @Override
    public boolean isNull() {
        return expression == null;
    }

    @Override
    public boolean isBlank() {
        return expression == null || expression.trim().isEmpty();
    }

    @Override
    public String getValue() {
        return expression;
    }

    @Override
    public int compareTo(String other) {
        return compareVersions(expression, other);
    }

    @Override
    public int compareTo(NVersion other) {
        return compareTo(other == null ? null : other.getValue());
    }

    @Override
    public NVersionFilter filter(NSession session) {
        return NVersionFilters.of(session).byValue(expression).get(session);
    }

    @Override
    public NVersion compatNewer() {
        String v = toExplicitSingleValueOrNullString();
        if (v == null) {
            return this;
        }
        return new DefaultNVersion("[" + expression + ",[");
    }

    @Override
    public NVersion compatOlder() {
        String v = toExplicitSingleValueOrNullString();
        if (v == null) {
            return this;
        }
        return new DefaultNVersion("]," + v + "]");
    }

    @Override
    public NOptional<List<NVersionInterval>> intervals() {
        return NVersionInterval.ofList(expression);
    }

    public NOptional<String> asSingleValue() {
        if (VERSION_PART_PATTERN.matcher(expression).matches()) {
            return NOptional.of(expression);
        }
        int commas = 0;
        int seps = 0;
        String s = expression.trim();
        NOptional<String> emptyVersion = NOptional.ofEmpty(session -> NMsg.ofC("not a single value : %s", expression));
        if (s.isEmpty()) {
            return emptyVersion;
        }
        for (char c : s.toCharArray()) {
            switch (c) {
                case '*':
                    return emptyVersion;
                case ',': {
                    commas++;
                    if (commas > 1) {
                        return emptyVersion;
                    }
                    if (seps >= 2) {
                        return emptyVersion;
                    }
                    break;
                }
                case '(':
                case ')':
                case '[':
                case ']': {
                    seps++;
                    if (seps > 2) {
                        return emptyVersion;
                    }
                    break;
                }
                default: {
                    if (!Character.isWhitespace(c)) {
                        if (seps >= 2) {
                            return emptyVersion;
                        }
                    }
                }
            }
        }
        if (seps == 0) {
            if (commas == 0) {
                if (VERSION_PART_PATTERN.matcher(expression).matches()) {
                    return NOptional.of(expression.trim());
                }
            } else {
                Set<String> all = new HashSet<>(NStringUtils.split(s, ",", true, true));
                if (all.size() == 1) {
                    String one = all.stream().findAny().get();
                    if (VERSION_PART_PATTERN.matcher(one).matches()) {
                        return NOptional.of(one);
                    }
                }
            }
        } else if (seps == 2) {
            // this is now a simple version
            char o = s.charAt(0);
            char c = s.charAt(s.length() - 1);
            if (o == '(') {
                o = ']';
            }
            if (c == ')') {
                c = '[';
            }
            s = s.substring(1, s.length() - 1).trim();
            if (o == '[' && c == ']') {
                if (commas == 0) {
                    if (VERSION_PART_PATTERN.matcher(s).matches()) {
                        return NOptional.of(s);
                    }
                } else {
                    //commas==1
                    Set<String> two = new HashSet<>(NStringUtils.split(s, ",", true, false));
                    if (two.size() == 1) {
                        String one = two.stream().findAny().get();
                        if (VERSION_PART_PATTERN.matcher(one).matches()) {
                            return NOptional.of(one);
                        }
                    }
                }
            }
        }
        return emptyVersion;
    }

    @Override
    public boolean isSingleValue() {
        return asSingleValue().isPresent();
    }

    @Override
    public boolean isFilter() {
        for (char c : expression.toCharArray()) {
            switch (c) {
                case '*':
                case ',':
                case '(':
                case ')':
                case '[':
                case ']': {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public NVersion inc() {
        return inc(-1);
    }

    @Override
    public NVersion inc(int index) {
        return inc(index, 1);
    }

    @Override
    public NVersion inc(int index, long amount) {
        return new DefaultNVersion(incVersion(getValue(), index, amount));
    }

    @Override
    public NVersion inc(int index, BigInteger amount) {
        return new DefaultNVersion(incVersion(getValue(), index, amount));
    }

    public int size() {
        VersionParts parts = getParts();
        return parts.size();
    }

    @Override
    public int numberSize() {
        return getParts().getDigitCount();
    }

    public NLiteral[] split() {
        VersionParts parts = getParts();
        int size = parts.size();
        NLiteral[] all = new NLiteral[size];
        for (int i = 0; i < size; i++) {
            all[i] = NLiteral.of(parts.get(i).string);
        }
        return all;
    }

    public NOptional<NLiteral> get(int index) {
        VersionParts parts = getParts();
        int size = parts.size();
        if (index >= 0) {
            if (index < parts.size()) {
                return NOptional.of(NLiteral.of(parts.get(index).string));
            }
        } else {
            int x = size + index;
            if (x >= 0 && x < parts.size()) {
                return NOptional.of(NLiteral.of(parts.get(x).string));
            }
        }
        return NOptional.ofEmpty(session -> NMsg.ofC("version part not found : %s", index));
    }

    public NOptional<NLiteral> getNumber(int level) {
        VersionParts parts = getParts();
        int size = parts.getDigitCount();
        if (level >= 0) {
            VersionPart digit = parts.getDigit(level);
            return NOptional.of(
                    digit == null ? null : NLiteral.of(digit.string),
                    s -> NMsg.ofC("missing number at %s", level)
            );
        } else {
            int x = size + level;
            VersionPart digit = x >= 0 ? parts.getDigit(x) : null;
            return NOptional.of(
                    digit == null ? null : NLiteral.of(digit.string),
                    s -> NMsg.ofC("missing number at %s", level)
            );
        }
    }

    @Override
    public NFormat formatter(NSession session) {
        return NVersionFormat.of(session).setVersion(this);
    }

    private String toExplicitSingleValueOrNullString() {
        if (!isBlank() && !isFilter()) {
            return expression;
        }
        return null;
    }

    private VersionParts getParts() {
        if (parts == null) {
            parts = splitVersionParts2(getValue());
        }
        return parts;
    }

    @Override
    public int hashCode() {
        return expression != null ? expression.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultNVersion version = (DefaultNVersion) o;

        return expression != null ? expression.equals(version.expression) : version.expression == null;

    }

    @Override
    public String toString() {
        return expression == null ? "" : expression;
    }

    enum VersionPartType {
        NUMBER,
        QAL,
        SEPARATOR,
    }

    private static class VersionPart {

        String string;
        VersionPartType type;

        public VersionPart(String string, VersionPartType type) {
            this.string = string;
            this.type = type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(string, type);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VersionPart that = (VersionPart) o;
            return string.equalsIgnoreCase(that.string) && type == that.type;
        }

        @Override
        public String toString() {
            String name = type.name().toLowerCase();
            return name + "(" + string + ")";
        }

        public int compareTo(VersionPart v2) {
            VersionPart v1 = this;
            if (v1.equals(v2)) {
                return 0;
            }
            if (v1.type == VersionPartType.SEPARATOR && v2.type == VersionPartType.SEPARATOR) {
                //a dash usually precedes a qualifier, and is always less important than something preceded with a dot.
                if (v1.string.equals("-")) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (v1.type == VersionPartType.SEPARATOR) {
                return -1;
            } else if (v2.type == VersionPartType.SEPARATOR) {
                return 1;
            }

            if (v1.type == VersionPartType.NUMBER && v2.type == VersionPartType.NUMBER) {
                return new BigInteger(v1.string).compareTo(new BigInteger(v2.string));
            } else if (v1.type == VersionPartType.NUMBER) {
                return 1;
            } else if (v2.type == VersionPartType.NUMBER) {
                return -1;
            } else {
                //both are string...
                Integer q1 = getKnownQualifierIndex(v1.string);
                Integer q2 = getKnownQualifierIndex(v2.string);
                if (q1 != null && q2 != null) {
                    return q1.compareTo(q2);
                } else if (q1 != null) {
                    return -1;
                } else if (q2 != null) {
                    return 1;
                } else {
                    return v1.string.compareToIgnoreCase(v2.string);
                }
            }
        }
    }

    private static class VersionParts {

        List<VersionPart> all;

        public VersionParts(List<VersionPart> all) {
            this.all = all;
        }

        public VersionPart get(int index) {
            return all.get(index);
        }

        public int size() {
            return all.size();
        }

        public int getDigitCount() {
            int c = 0;
            for (VersionPart s : all) {
                if (s.type == VersionPartType.NUMBER) {
                    c++;
                }
            }
            return c;
        }

        public VersionPart getDigit(int index) {
            int c = 0;
            for (VersionPart s : all) {
                if (s.type == VersionPartType.NUMBER) {
                    if (c == index) {
                        return s;
                    }
                    c++;
                }
            }
            return null;
        }

        public void insertDigit(long val, String sep) {
            if (all.size() == 0) {
                all.add(new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            } else if (all.get(0).type == VersionPartType.NUMBER) {
                if (sep == null) {
                    sep = ".";
                }
                if (!sep.equals(".") && !sep.equals("-")) {
                    throw new IllegalArgumentException("illegal separator");
                }
                all.add(0, new VersionPart(sep, VersionPartType.SEPARATOR));
                all.add(0, new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            } else {
                all.add(0, new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            }
        }

        public void addDigit(BigInteger val, String sep) {
            if (all.size() == 0) {
                all.add(new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            } else if (all.get(all.size() - 1).type == VersionPartType.NUMBER) {
                if (sep == null) {
                    sep = ".";
                }
                if (!sep.equals(".") && !sep.equals("-")) {
                    throw new IllegalArgumentException("illegal separator");
                }
                all.add(new VersionPart(sep, VersionPartType.SEPARATOR));
                all.add(new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            } else {
                all.add(new VersionPart(String.valueOf(val), VersionPartType.NUMBER));
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (VersionPart versionPart : all) {
                sb.append(versionPart.string);
            }
            return sb.toString();
        }

        /**
         * https://maven.apache.org/ref/3.3.3/maven-artifact/apidocs/org/apache/maven/artifact/versioning/ComparableVersion.html
         *
         * @param v2 v2
         * @return compare int
         */
        public int compareTo(VersionParts v2) {
            VersionParts v1 = this;
            int i = 0;
            int j = 0;
            while (i < v1.size() || j < v2.size()) {
                if (i < v1.size() && j < v2.size()) {
                    VersionPart a = v1.get(i);
                    VersionPart b = v2.get(i);
                    int r = a.compareTo(b);
                    if (r != 0) {
                        return r;
                    }
                    i++;
                    j++;
                } else if (i < v1.size()) {
                    if (isQualifierFrom(i, v1)) {
                        return -1;
                    }
                    return 1;
                } else {
                    if (isQualifierFrom(i, v2)) {
                        return 1;
                    }
                    return -1;
                }
            }
            return 0;
        }
    }

    private static boolean isQualifierFrom(int i, VersionParts v1) {
        VersionPart a = v1.get(i);
        if (a.type == VersionPartType.SEPARATOR && a.string.equals("-")) {
            if (i + 1 < v1.size()) {
                for (int j = i + 1; j < v1.size(); j++) {
                    a = v1.get(j);
                    switch (a.type) {
                        case SEPARATOR:
                            if (a.string.equals(".")) {
                                return false;
                            }
                            break;
                        case QAL:
                            return true;
                        default:
                            return false;
                    }
                }
                return true;
            } else {
                return true;
            }
        }
        return false;
    }


}
