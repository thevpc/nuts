/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.terminals.textparsers;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.extensions.terminals.NutsTextNode;
import net.vpc.app.nuts.extensions.terminals.textparsers.DefaultNutsTextParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vpc
 */
public class TextParserTest {

    public TextParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void case1() throws ParseException {
        String text = "``3052`` ok ``2``";
        NutsDefaultParserImpl d = new NutsDefaultParserImpl(new StringReader(text));
        TextNode r = d.parseList();
        TextNode y;
        y = DefaultNutsTextParser.INSTANCE.parseTextNode(text);
        display(y, "");
    }

//    @Test
//    public void case2() throws ParseException {
//        String text = "Nuts loaded in [[``3052``]] ms (boot in [[``2``]] ms, create workspace in [[``3002``]] ms))";
//        TextNode y;
//        y = DefaultNutsTextParser.INSTANCE.parseTextNode(text);
//        display(y, "");
//    }

    protected void display(TextNode n, String prefix) {
        System.out.printf(prefix + "%s : %s\n", n.getType(), n.getValue());
        for (TextNode node : n.getNodes()) {
            display(node, prefix + "  ");
        }
    }
}
