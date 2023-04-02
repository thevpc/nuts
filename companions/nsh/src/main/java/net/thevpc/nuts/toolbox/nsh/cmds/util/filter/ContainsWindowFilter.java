/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nsh.cmds.util.filter;

import net.thevpc.nuts.toolbox.nsh.cmds.util.WindowFilter;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author vpc
 */
public class ContainsWindowFilter implements WindowFilter<String> {

    private final String match;
    private final String matchString;
    private final boolean caseSensitive;

    public ContainsWindowFilter(String match, boolean caseSensitive) {
        this.match = caseSensitive ? match : match.toLowerCase();
        this.caseSensitive = caseSensitive;
        this.matchString = match;
    }

    @Override
    public boolean accept(String line) {
        String text = line;
        if (!caseSensitive) {
            text = text.toLowerCase();
        }
        return text.contains(match);
    }

    @Override
    public WindowFilter<String> copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        if (caseSensitive) {
            return "Contains(" + NStringUtils.formatStringLiteral(matchString) + ')';
        } else {
            return "ContainsInsensitive(" + NStringUtils.formatStringLiteral(matchString) + ')';
        }
    }


}
