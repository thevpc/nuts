/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.processors;

import net.thevpc.nuts.toolbox.ncode.SourceProcessor;
import net.thevpc.nuts.toolbox.ncode.Source;

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
