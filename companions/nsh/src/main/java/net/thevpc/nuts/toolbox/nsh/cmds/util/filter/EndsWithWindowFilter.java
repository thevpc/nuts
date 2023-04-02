/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nsh.cmds.util.filter;

import net.thevpc.nuts.toolbox.nsh.cmds.util.WindowFilter;
import net.thevpc.nuts.toolbox.nsh.cmds.util.NNumberedObject;
import net.thevpc.nuts.util.NStringUtils;

/**
 * @author vpc
 */
public class EndsWithWindowFilter implements WindowFilter<NNumberedObject<String>> {

    private final String match;
    private final String matchString;
    private final boolean caseSensitive;

    public EndsWithWindowFilter(String match, boolean caseSensitive) {
        this.matchString = match;
        this.match = caseSensitive ? match : match.toLowerCase();
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean accept(NNumberedObject<String> line) {
        String text = line.getObject();
        if (!caseSensitive) {
            text = text.toLowerCase();
        }
        return text.endsWith(match);
    }

    @Override
    public WindowFilter<NNumberedObject<String>> copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        if (caseSensitive) {
            return "EndsWith(" + NStringUtils.formatStringLiteral(matchString) + ')';
        } else {
            return "EndsWithInsensitive(" + NStringUtils.formatStringLiteral(matchString) + ')';
        }
    }
}
