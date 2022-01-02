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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.version;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNumberUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.version.filter.DefaultNutsVersionFilter;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNutsVersion /*extends DefaultNutsTokenFilter*/ implements NutsVersion {

    private static final long serialVersionUID = 1L;
    private final transient NutsSession session;
    protected String expression;
    private VersionParts parts;

    public DefaultNutsVersion(String expression, NutsSession session) {
        this.expression = (NutsUtilStrings.trim(expression));
        this.session = session;
    }

    public static boolean versionMatches(String version, String pattern) {
        if (isBlankVersion(pattern)) {
            return true;
        }
        return pattern.equals(version);
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
        v1 = NutsUtilStrings.trim(v1);
        v2 = NutsUtilStrings.trim(v2);
        if (v1.equals(v2)) {
            return 0;
        }
        if (NutsConstants.Versions.LATEST.equals(v1)) {
            return 1;
        }
        if (NutsConstants.Versions.LATEST.equals(v2)) {
            return -1;
        }
        if (NutsConstants.Versions.RELEASE.equals(v1)) {
            return 1;
        }
        if (NutsConstants.Versions.RELEASE.equals(v2)) {
            return -1;
        }
        VersionParts v1arr = splitVersionParts2(v1);
        VersionParts v2arr = splitVersionParts2(v2);
        return v1arr.compareTo(v2arr);
    }


    private static VersionParts splitVersionParts2(String v1) {
        v1 = NutsUtilStrings.trim(v1);
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
                    CoreStringUtils.clear(last);
                    partType = VersionPartType.NUMBER;
                    last.append(c);
                }
            } else if (c == '.' || c == '-') {
                if (last == null) {
                    parts.add(new VersionPart(String.valueOf(c), VersionPartType.SEPARATOR));
                    partType = VersionPartType.SEPARATOR;
                } else if (partType == VersionPartType.NUMBER) {
                    parts.add(new VersionPart(last.toString(), partType));
                    last = null;
                    partType = VersionPartType.SEPARATOR;
                } else {
                    parts.add(new VersionPart(last.toString(), partType));
                    last = null;
                    partType = VersionPartType.SEPARATOR;
                }
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
                    CoreStringUtils.clear(last);
                    last.append(c);
                }
            }
        }
        if (last != null && last.length() > 0) {
            parts.add(new VersionPart(last.toString(), partType));
        }
        return new VersionParts(parts);
    }



    public static Integer getKnownQualifierIndex(String v1) {
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





    public static boolean isBlankVersion(String pattern) {
        if (NutsBlankable.isBlank(pattern)) {
            return true;
        }
        return NutsConstants.Versions.LATEST.equals(pattern) || NutsConstants.Versions.RELEASE.equals(pattern);
    }

    public static boolean isStaticVersionPattern(String pattern) {
        if (isBlankVersion(pattern)) {
            return false;
        }
        return !pattern.contains("[") && !pattern.contains("]") && !pattern.contains(",") && !pattern.contains("*");
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
    public int compareTo(NutsVersion other) {
        return compareTo(other == null ? null : other.getValue());
    }

    @Override
    public NutsVersionFilter filter() {
        return DefaultNutsVersionFilter.parse(expression, session);
    }

    @Override
    public NutsVersion compatNewer() {
        String v = toExplicitSingleValueOrNullString();
        if (v == null) {
            return this;
        }
        return new DefaultNutsVersion("[" + expression + ",[", session);
    }

    @Override
    public NutsVersion compatOlder() {
        String v = toExplicitSingleValueOrNullString();
        if (v == null) {
            return this;
        }
        return new DefaultNutsVersion("]," + v + "]", session);
    }

    @Override
    public NutsVersionInterval[] intervals() {
        NutsVersionFilter s = filter();
        if (s instanceof DefaultNutsVersionFilter) {
            return ((DefaultNutsVersionFilter) s).getIntervals();
        }
        return new NutsVersionInterval[0];
    }

    @Override
    public boolean isSingleValue() {
        NutsVersionInterval[] nutsVersionIntervals = intervals();
        return nutsVersionIntervals.length != 0 && nutsVersionIntervals.length <= 1 && nutsVersionIntervals[0].isFixedValue();
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
    public NutsVersion inc() {
        return inc(-1);
    }

    @Override
    public NutsVersion inc(int position) {
        return inc(position, 1);
    }

    @Override
    public NutsVersion inc(int position, long amount) {
        return new DefaultNutsVersion(incVersion(getValue(), position, amount), session);
    }

    @Override
    public NutsVersion inc(int position, BigInteger amount) {
        return new DefaultNutsVersion(incVersion(getValue(), position, amount), session);
    }

    public int size() {
        VersionParts parts = getParts();
        return parts.size();
    }

    @Override
    public int numberSize() {
        return getParts().getDigitCount();
    }

    public String get(int level) {
        VersionParts parts = getParts();
        int size = parts.size();
        if (level >= 0) {
            return parts.get(level).string;
        } else {
            int x = size + level;
            return parts.get(x).string;
        }
    }

    public BigInteger getNumber(int level) {
        VersionParts parts = getParts();
        int size = parts.getDigitCount();
        if (level >= 0) {
            return new BigInteger(parts.getDigit(level).string);
        } else {
            int x = size + level;
            return new BigInteger(parts.getDigit(x).string);
        }
    }

    public BigInteger getNumber(int level, BigInteger defaultValue) {
        VersionParts parts = getParts();
        int size = parts.getDigitCount();
        if (level >= 0) {
            if (level < size) {
                return new BigInteger(parts.getDigit(level).string);
            }
        } else {
            int x = size + level;
            if (x < size) {
                return new BigInteger(parts.getDigit(x).string);
            }
        }
        return defaultValue;
    }

//    @Override
//    public boolean matches(String expression) {
//        if (NutsBlankable.isBlank(expression)) {
//            return true;
//        }
//        return DefaultNutsVersionFilter.parse(expression,session).acceptVersion(this, session);
//    }

    @Override
    public int getInt(int index, int defaultValue) {
        return getNumber(index, BigInteger.valueOf(defaultValue)).intValue();
    }

    @Override
    public long getLong(int index, long defaultValue) {
        return getNumber(index, BigInteger.valueOf(defaultValue)).longValue();
    }

    @Override
    public NutsFormat formatter() {
        return NutsVersionFormat.of(session).setVersion(this);
    }

    private String toExplicitSingleValueOrNullString() {
        if (!isBlank() && !isFilter()) {
            return expression;
        }
        return null;
    }

    private String toSingleValueOrNullString() {
        NutsVersionInterval[] nutsVersionIntervals = intervals();
        if (nutsVersionIntervals.length == 1) {
            if (nutsVersionIntervals[0].isFixedValue()) {
                return nutsVersionIntervals[0].getLowerBound();
            }
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

        DefaultNutsVersion version = (DefaultNutsVersion) o;

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
            VersionPart v1=this;
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
                return CoreNumberUtils.convertToBigInteger(v1.string, null)
                        .compareTo(
                                CoreNumberUtils.convertToBigInteger(v2.string, null)
                        );
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
            VersionParts v1=this;
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
                    VersionPart a = v1.get(i);
                    if (a.type == VersionPartType.NUMBER || a.type == VersionPartType.SEPARATOR) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    VersionPart b = v2.get(i);
                    if (b.type == VersionPartType.NUMBER || b.type == VersionPartType.SEPARATOR) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
            return 0;
        }
    }
}
