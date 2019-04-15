/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.testutils;

import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.io.InputSource;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test01_TestInputSource {

    @Test
    public void test1() throws Exception {
        InputSource s = CoreIOUtils.createInputSource("http://maven.ibiblio.org/maven2/archetype-catalog.xml");
        
        Assert.assertFalse(s.isPath());
        Assert.assertTrue(s.isURL());
        
        s = CoreIOUtils.createInputSource("file://maven.ibiblio.org/maven2/archetype-catalog.xml");
        Assert.assertTrue(s.isPath());
        Assert.assertTrue(s.isURL());
        
        s = CoreIOUtils.createInputSource("file:/maven.ibiblio.org/maven2/archetype-catalog.xml");
        Assert.assertTrue(s.isPath());
        Assert.assertTrue(s.isURL());
        
//        s = CoreIOUtils.createInputSource("zip://maven.ibiblio.org/maven2/toto.zip?archetype-catalog.xml");
//        Assert.assertFalse(s.isPath());
//        Assert.assertTrue(s.isURL());

        s = CoreIOUtils.createInputSource("/maven.ibiblio.org/maven2/archetype-catalog.xml");
        Assert.assertTrue(s.isPath());
        Assert.assertTrue(s.isURL());
        
    }

}
