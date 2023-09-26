/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nsh.cmds.util.filter;

import net.thevpc.nuts.toolbox.nsh.cmds.util.FilterResult;
import net.thevpc.nuts.toolbox.nsh.cmds.util.WindowFilter;
import net.thevpc.nuts.toolbox.nsh.cmds.util.NNumberedObject;
import net.thevpc.nuts.util.NBlankable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vpc
 */
public class JavaExceptionWindowFilter implements WindowFilter<NNumberedObject<String>> {

    private final Pattern atPattern = Pattern.compile("[ \t]at [a-z]+[.].*");
    private final Pattern javaPattern = Pattern.compile("[.]java:[0-9]+[)]");
    private int rows;
    private List<JexFilter> jexFilters = new ArrayList<>();
    private String minDate;
    private String maxDate;

    DateFormatHelper[] fs = new DateFormatHelper[]{
            new DateFormatHelper("yyyy-MM-dd HH:mm:ss.SSS")
            , new DateFormatHelper("yyyy-MM-dd HH:mm:ss")
    };

    public JavaExceptionWindowFilter() {
    }

    public String getMinDate() {
        return minDate;
    }

    public JavaExceptionWindowFilter setMinDate(String minDate) {
        this.minDate = minDate;
        return this;
    }

    public String getMaxDate() {
        return maxDate;
    }

    public JavaExceptionWindowFilter setMaxDate(String maxDate) {
        this.maxDate = maxDate;
        return this;
    }

    public List<JexFilter> getJexFilters() {
        return jexFilters;
    }

    public int getRows() {
        return rows;
    }

    public JavaExceptionWindowFilter setRows(int rows) {
        this.rows = rows;
        return this;
    }

    @Override
    public boolean accept(NNumberedObject<String> line) {
        String text = line.getObject();
        if (atPattern.matcher(text).matches()) {
            return true;
        }
        if (javaPattern.matcher(text).matches()) {
            return true;
        }
        return false;
    }

    @Override
    public WindowFilter<NNumberedObject<String>> copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        return "JavaException";
    }

    @Override
    public boolean acceptPrevious(NNumberedObject<String> line, NNumberedObject<String> pivot, int pivotIndex, List<NNumberedObject<String>> all) {
        if (pivot.getNumber() == pivot.getNumber() - 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean acceptNext(NNumberedObject<String> line, NNumberedObject<String> pivot, int pivotIndex, List<NNumberedObject<String>> all) {
        return accept(line);
    }

    @Override
    public int getPreviousWindowSize() {
        return 1;
    }

    @Override
    public int getNextWindowSize() {
        return 1000;
    }


    @Override
    public void prepare(List<NNumberedObject<String>> all, int pivotIndex) {
        FilterResult r = null;
        for (NNumberedObject<String> a : all) {
            r = combine(r, acceptDate(extractDate(a.getObject())));
            if(r==FilterResult.REJECT){
                break;
            }
            for (JexFilter jexFilter : jexFilters) {
                r = combine(r, applyJexFilterMatch(a,jexFilter));
                if(r==FilterResult.REJECT){
                    break;
                }
            }
            if(r==FilterResult.REJECT){
                break;
            }
        }
        if (r == FilterResult.REJECT) {
            all.clear();
        }
        if (rows > 0) {
            while (all.size() > rows) {
                all.remove(all.size() - 1);
            }
        }
    }

    private FilterResult applyJexFilterMatch(NNumberedObject<String> e,JexFilter jexFilter) {
        String s = e.getObject();
        if (containsMatch(s, jexFilter.regexp)) {
            if(jexFilter.include) {
                return FilterResult.ACCEPT;
            }else{
                return FilterResult.REJECT;
            }
        }
        return FilterResult.NEUTRAL;
    }

    private Instant extractDate(String line) {
        if (line != null) {
            for (DateFormatHelper format : fs) {
                Instant i = format.extract(line);
                if (i != null) {
                    return i;
                }
            }
        }
        return null;
    }

    private FilterResult acceptDate(Instant instant) {
        if (instant == null) {
            return FilterResult.NEUTRAL;
        }
        boolean neutral = true;
        if (!NBlankable.isBlank(minDate)) {
            if (instant.isBefore(extractDate(minDate.trim()))) {
                return FilterResult.REJECT;
            } else {
                neutral = false;
            }
        }
        if (!NBlankable.isBlank(maxDate)) {
            if (instant.isAfter(extractDate(maxDate.trim()))) {
                return FilterResult.REJECT;
            } else {
                neutral = false;
            }
        }
        if (neutral) {
            return FilterResult.NEUTRAL;
        } else {
            return FilterResult.ACCEPT;
        }
    }

    private boolean containsMatch(String s, String portion) {
        Pattern p = Pattern.compile(portion);
        Matcher m = p.matcher(s);
        if (m.find()) {
            return true;
        }
        return false;
    }

    private FilterResult combine(FilterResult a, FilterResult b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a == FilterResult.NEUTRAL) {
            return b;
        }
        if (b == FilterResult.NEUTRAL) {
            return a;
        }
        if (a == b) {
            return a;
        }
        return FilterResult.REJECT;
    }

    private static class DateFormatHelper {
        private String format;
        private Pattern findPattern;
        private SimpleDateFormat df;

        public DateFormatHelper(String format) {
            this.format = format;
            df = new SimpleDateFormat(format);
            StringBuilder sb = new StringBuilder();
            for (char c : format.toCharArray()) {
                switch (c) {
                    case 'y':
                    case 'Y':
                    case 'M':
                    case 'm':
                    case 's':
                    case 'S':
                    case 'd':
                    case 'H': {
                        sb.append("\\d");
                        break;
                    }
                    case '-':
                    case '/':
                    case ' ':
                    case ':': {
                        sb.append(c);
                        break;
                    }
                    case '.': {
                        sb.append("[.]");
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("unsupported date format char " + c);
                    }
                }
            }
            findPattern = Pattern.compile(sb.toString());
        }

        public Instant extract(String line) {
            Matcher m = findPattern.matcher(line);
            if (m.find()) {
                try {
                    return df.parse(m.group()).toInstant();
                } catch (ParseException e) {
                    //
                }
            }
            return null;
        }
    }

    public static class JexFilter {
        String regexp;
        boolean include;

        public JexFilter(String regexp, boolean include) {
            this.regexp = regexp;
            this.include = include;
        }
    }
}
