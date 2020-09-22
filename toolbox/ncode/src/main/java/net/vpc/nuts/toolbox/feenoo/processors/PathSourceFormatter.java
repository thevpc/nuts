/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.nuts.toolbox.feenoo.processors;

import net.vpc.nuts.toolbox.feenoo.Source;
import net.vpc.nuts.toolbox.feenoo.SourceProcessor;

/**
 * @author vpc
 */
public class PathSourceFormatter implements SourceProcessor {

    public PathSourceFormatter() {
    }

    @Override
    public void process(Source source) {
        System.out.println(source.getExternalPath());
    }

}
