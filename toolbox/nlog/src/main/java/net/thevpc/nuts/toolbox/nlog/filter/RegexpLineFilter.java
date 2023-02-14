/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog.filter;


import net.thevpc.nuts.toolbox.nlog.model.Line;
import net.thevpc.nuts.toolbox.nlog.model.LineFilter;
import net.thevpc.nuts.toolbox.nlog.util.StringUtils;

import java.util.regex.Pattern;

/**
 *
 * @author vpc
 */
public class RegexpLineFilter implements LineFilter {

    private final String matchString;
    private final Pattern match;
    private final boolean caseSensitive;

    public RegexpLineFilter(String match, boolean caseSensitive) {
        this.matchString=match;
        this.match = Pattern.compile(((caseSensitive ? "" : "(?i)") + match));
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean accept(Line line) {
        return match.matcher(line.getText()).matches();
    }

    @Override
    public LineFilter copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        if (caseSensitive) {
            return "RegExp(" + StringUtils.quote(matchString) + ')';
        } else {
            return "RegExpInsensitive(" + StringUtils.quote(matchString) + ')';
        }
    }
}
