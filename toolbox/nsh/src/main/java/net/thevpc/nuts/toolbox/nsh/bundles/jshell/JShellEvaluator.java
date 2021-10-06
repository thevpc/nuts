/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

/**
 *
 * @author thevpc
 */
public interface JShellEvaluator {

    int evalSuffixOperation(String opString, JShellCommandNode node, JShellContext context);

    int evalSuffixAndOperation(JShellCommandNode node, JShellContext context);

    int evalBinaryAndOperation(JShellCommandNode left, JShellCommandNode right, JShellContext context);

    int evalBinaryOperation(String opString, JShellCommandNode left, JShellCommandNode right, JShellContext context);

    int evalBinaryOrOperation(JShellCommandNode left, JShellCommandNode right, JShellContext context);

    int evalBinaryPipeOperation(JShellCommandNode left, JShellCommandNode right, final JShellContext context);

    int evalBinarySuiteOperation(JShellCommandNode left, JShellCommandNode right, JShellContext context);

    String evalCommandAndReturnString(JShellCommandNode left, JShellContext context);


    String evalDollarSharp(JShellContext context);

    String evalDollarName(String name, JShellContext context);

    String evalDollarInterrogation(JShellContext context);

    String evalDollarInteger(int index, JShellContext context);

    String evalDollarExpression(String stringExpression, JShellContext context);

    String evalSimpleQuotesExpression(String expressionString, JShellContext context);

    String evalDoubleQuotesExpression(String stringExpression, JShellContext context);

    String evalAntiQuotesExpression(String stringExpression, JShellContext context);

    String evalNoQuotesExpression(String stringExpression, JShellContext context);

    String expandEnvVars(String stringExpression, boolean escapeResultPath, JShellContext context);

}
