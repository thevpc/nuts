/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.processors;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ncode.SourceProcessor;
import net.thevpc.nuts.toolbox.ncode.Source;
import net.thevpc.nuts.toolbox.ncode.sources.JavaTypeSource;

/**
 * @author thevpc
 */
public class JavaSourceFormatter implements SourceProcessor {

    private int clsNameSize = 20;

    public JavaSourceFormatter() {
    }

    @Override
    public Object process(Source source, NSession session) {
        if (source instanceof JavaTypeSource) {
            JavaTypeSource s = (JavaTypeSource) source;
            String v1 = s.getClassVersion(false);
            String v2 = s.getClassVersion(true);
            String n = s.getClassName();
            if (n.length() > clsNameSize) {
                clsNameSize = n.length();
            }
            return NMsg.ofC(
                    "%s %s %s %s",
                    NTexts.of(session).ofStyled(leftAlign(v1, 4), NTextStyle.config()),
                    NTexts.of(session).ofStyled(leftAlign(v2, 4), NTextStyle.info()),
                    NTexts.of(session).ofStyled(leftAlign(n, clsNameSize), NTextStyle.primary1()),
                    NTexts.of(session).ofStyled(source.getExternalPath(), NTextStyle.path()),
                    source.toString()
            );
        } else {
            return NMsg.ofC(
                    "%s : %s",
                    NTexts.of(session).ofStyled("invalid source", NTextStyle.error()),
                    source.toString()
            );
        }
    }

    private String leftAlign(String n, int size) {
        StringBuilder sb = new StringBuilder(size);
        sb.append(n);
        int x = size - n.length();
        while (x > 0) {
            sb.append(' ');
            x--;
        }
        return sb.toString();
    }

}
