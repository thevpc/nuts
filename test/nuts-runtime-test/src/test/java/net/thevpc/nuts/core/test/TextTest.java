/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author thevpc
 */
public class TextTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }



    @Test
    public void test1() {
        List<NText> split = NText.ofList(NText.of("a/:b"), NText.of("/"), NText.of("c"), NText.of("/"))
                .split("/:",true);
        TestUtils.println(split);
        split = NText.of(NText.of("a/:b/c/"))
                .split("/:",true);
        TestUtils.println(split);
    }

    @Test
    public void testVisitDFS() {
        NText tree = NText.ofList(
                NText.ofStyled(NText.ofPlain("hello"), NTextStyle.error()),
                NText.ofTitle(NText.ofPlain("world"), 1),
                NTextBuilder.of().append(NText.ofPlain("child1")).append(NText.ofPlain("child2")).build()
        );

        List<String> events = new ArrayList<>();
        NText.visitDFS(tree, new NTextVisitor() {
            @Override
            public void enter(NText node) {
                events.add("enter:" + node.type() + (node instanceof NTextPlain ? "(" + ((NTextPlain) node).value() + ")" : ""));
            }

            @Override
            public void exit(NText node) {
                events.add("exit:" + node.type() + (node instanceof NTextPlain ? "(" + ((NTextPlain) node).value() + ")" : ""));
            }
        });

        List<String> expected = Arrays.asList(
                "enter:LIST",
                "enter:STYLED",
                "enter:PLAIN(hello)",
                "exit:PLAIN(hello)",
                "exit:STYLED",
                "enter:TITLE",
                "enter:PLAIN(world)",
                "exit:PLAIN(world)",
                "exit:TITLE",
                "enter:BUILDER",
                "enter:PLAIN(child1)",
                "exit:PLAIN(child1)",
                "enter:PLAIN(child2)",
                "exit:PLAIN(child2)",
                "exit:BUILDER",
                "exit:LIST"
        );
        Assertions.assertEquals(expected, events);

        // test null
        List<String> nullEvents = new ArrayList<>();
        NTextVisitor nullVisitor = new NTextVisitor() {
            @Override
            public void enter(NText node) {
                nullEvents.add("enter");
            }
        };
        NText.visitDFS((NText) null, nullVisitor);
        Assertions.assertTrue(nullEvents.isEmpty());
    }

    @Test
    public void testVisitBFS() {
        NText tree = NText.ofList(
                NText.ofStyled(NText.ofPlain("hello"), NTextStyle.error()),
                NText.ofTitle(NText.ofPlain("world"), 1),
                NTextBuilder.of().append(NText.ofPlain("child1")).append(NText.ofPlain("child2")).build()
        );

        List<String> events = new ArrayList<>();
        NText.visitBFS(tree, new NTextVisitor() {
            @Override
            public void enter(NText node) {
                events.add("enter:" + node.type() + (node instanceof NTextPlain ? "(" + ((NTextPlain) node).value() + ")" : ""));
            }

            @Override
            public void exit(NText node) {
                events.add("exit:" + node.type() + (node instanceof NTextPlain ? "(" + ((NTextPlain) node).value() + ")" : ""));
            }
        });

        List<String> expected = Arrays.asList(
                "enter:LIST",
                "enter:STYLED",
                "enter:TITLE",
                "enter:BUILDER",
                "enter:PLAIN(hello)",
                "enter:PLAIN(world)",
                "enter:PLAIN(child1)",
                "enter:PLAIN(child2)",
                "exit:PLAIN(child2)",
                "exit:PLAIN(child1)",
                "exit:PLAIN(world)",
                "exit:PLAIN(hello)",
                "exit:BUILDER",
                "exit:TITLE",
                "exit:STYLED",
                "exit:LIST"
        );
        Assertions.assertEquals(expected, events);

        // test null
        List<String> nullEvents = new ArrayList<>();
        NTextVisitor nullVisitor = new NTextVisitor() {
            @Override
            public void enter(NText node) {
                nullEvents.add("enter");
            }
        };
        NText.visitBFS((NText) null, nullVisitor);
        Assertions.assertTrue(nullEvents.isEmpty());
    }
}
