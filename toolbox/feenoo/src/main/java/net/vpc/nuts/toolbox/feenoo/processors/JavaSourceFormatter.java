/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.nuts.toolbox.feenoo.processors;

import net.vpc.nuts.toolbox.feenoo.Source;
import net.vpc.nuts.toolbox.feenoo.SourceProcessor;
import net.vpc.nuts.toolbox.feenoo.sources.JavaTypeSource;

/**
 * @author vpc
 */
public class JavaSourceFormatter implements SourceProcessor {

    private int clsNameSize = 20;

    public JavaSourceFormatter() {
    }

    @Override
    public void process(Source source) {
        if (source instanceof JavaTypeSource) {
            JavaTypeSource s = (JavaTypeSource) source;
            String v1 = s.getClassVersion(false);
            String v2 = s.getClassVersion(true);
            String n = s.getClassName();
            if (n.length() > clsNameSize) {
                clsNameSize = n.length();
            }
            System.out.println(leftAlign(v1, 4) + " " + leftAlign(v2, 4) + " " + leftAlign(n, clsNameSize) + " " + source.getExternalPath());
        } else {
            System.out.println("Invalid source : " + source);
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
