/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.nuts.toolbox.feenoo.sources;


import net.vpc.nuts.toolbox.feenoo.Source;

/**
 *
 * @author vpc
 */
public class JavaTypeSource extends SourceAdater {

    public JavaTypeSource(Source source) {
        super(source);
    }

    public String getClassName() {
        String ii = getInternalPath();
        return ii.substring(0,ii.length()-".class".length()).replace("/", ".");
    }

}
