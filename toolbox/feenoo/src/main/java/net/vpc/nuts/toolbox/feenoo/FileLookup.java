/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.nuts.toolbox.feenoo;

import net.vpc.common.strings.StringComparator;

/**
 *
 * @author vpc
 */
class FileLookup implements SourceProcessor {

    private final StringComparator comparator;

    public FileLookup(StringComparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean process(Source source) {
        if (comparator.matches(source.getExternalPath())) {
            System.out.println(source.getExternalPath());
        }
        return true;
    }

}
