/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nsh.cmds.util.filter;


import net.thevpc.nuts.toolbox.nsh.cmds.util.WindowFilter;
import net.thevpc.nuts.toolbox.nsh.cmds.util.NNumberedObject;
import net.thevpc.nuts.util.NStringUtils;

import java.util.regex.Pattern;

/**
 *
 * @author vpc
 */
public class RegexpWindowFilter implements WindowFilter<NNumberedObject<String>> {

    private final String matchString;
    private final Pattern match;
    private final boolean caseSensitive;

    public RegexpWindowFilter(String match, boolean caseSensitive) {
        this.matchString=match;
        this.match = Pattern.compile(((caseSensitive ? "" : "(?i)") + match));
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean accept(NNumberedObject<String> line) {
        return match.matcher(line.getObject()).matches();
    }

    @Override
    public WindowFilter<NNumberedObject<String>> copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        if (caseSensitive) {
            return "RegExp(" + NStringUtils.formatStringLiteral(matchString) + ')';
        } else {
            return "RegExpInsensitive(" + NStringUtils.formatStringLiteral(matchString) + ')';
        }
    }
}
