/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog.filter;

import net.thevpc.nuts.toolbox.nlog.model.LineFilter;
import net.thevpc.nuts.toolbox.nlog.model.Line;
import net.thevpc.nuts.toolbox.nlog.util.StringUtils;

/**
 *
 * @author vpc
 */
public class EndsWithLineFilter implements LineFilter {

    private final String match;
    private final String matchString;
    private final boolean caseSensitive;

    public EndsWithLineFilter(String match, boolean caseSensitive) {
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
        return text.endsWith(match);
    }

    @Override
    public LineFilter copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        if (caseSensitive) {
            return "EndsWith(" + StringUtils.quote(matchString) + ')';
        } else {
            return "EndsWithInsensitive(" + StringUtils.quote(matchString) + ')';
        }
    }
}
