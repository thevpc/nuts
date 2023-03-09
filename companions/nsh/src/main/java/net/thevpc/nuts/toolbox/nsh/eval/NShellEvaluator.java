/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.eval;

import net.thevpc.nuts.toolbox.nsh.nodes.NShellCommandNode;

/**
 *
 * @author thevpc
 */
public interface NShellEvaluator {

    int evalSuffixOperation(String opString, NShellCommandNode node, NShellContext context);

    int evalSuffixAndOperation(NShellCommandNode node, NShellContext context);

    int evalBinaryAndOperation(NShellCommandNode left, NShellCommandNode right, NShellContext context);

    int evalBinaryOperation(String opString, NShellCommandNode left, NShellCommandNode right, NShellContext context);

    int evalBinaryOrOperation(NShellCommandNode left, NShellCommandNode right, NShellContext context);

    int evalBinaryPipeOperation(NShellCommandNode left, NShellCommandNode right, final NShellContext context);

    int evalBinarySuiteOperation(NShellCommandNode left, NShellCommandNode right, NShellContext context);

    String evalCommandAndReturnString(NShellCommandNode left, NShellContext context);


    String evalDollarSharp(NShellContext context);

    String evalDollarName(String name, NShellContext context);

    String evalDollarInterrogation(NShellContext context);

    String evalDollarInteger(int index, NShellContext context);

    String evalDollarExpression(String stringExpression, NShellContext context);

    String evalSimpleQuotesExpression(String expressionString, NShellContext context);

    String evalDoubleQuotesExpression(String stringExpression, NShellContext context);

    String evalAntiQuotesExpression(String stringExpression, NShellContext context);

    String evalNoQuotesExpression(String stringExpression, NShellContext context);

    String expandEnvVars(String stringExpression, boolean escapeResultPath, NShellContext context);

}
