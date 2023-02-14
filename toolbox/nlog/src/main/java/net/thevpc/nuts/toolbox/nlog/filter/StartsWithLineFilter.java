/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog.filter;

import net.thevpc.nuts.toolbox.nlog.model.Line;
import net.thevpc.nuts.toolbox.nlog.model.LineFilter;
import net.thevpc.nuts.toolbox.nlog.util.StringUtils;

/**
 *
 * @author vpc
 */
public class StartsWithLineFilter implements LineFilter {

    private final String match;
    private final String matchString;
    private final boolean caseSensitive;

    public StartsWithLineFilter(String match, boolean caseSensitive) {
        this.matchString = match;
        this.match = caseSensitive ? match : match.toLowerCase();
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean accept(Line line) {
        String text = line.getText();
        if (!caseSensitive) {
            text = text.toLowerCase();
        }
        return text.startsWith(match);
    }

    @Override
    public LineFilter copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        if (caseSensitive) {
            return "StartsWith(" + StringUtils.quote(matchString) + ')';
        } else {
            return "StartsWith(" + StringUtils.quote(matchString) + ')';
        }
    }

}
