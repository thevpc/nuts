/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.deprecated;


/**
 *
 * @author thevpc
 */
public class MessageNameFormatFactory {

    public static final MessageNameFormat.Function FCT_SWITCH = new SwitchFunction();

    public static final MessageNameFormat.Function FCT_INTEGER = new IntegerFunction();

    public static final MessageNameFormat.Function FCT_DOUBLE = new DoubleFunction();

    public static final MessageNameFormat.Function FCT_DATE = new DateFunction();
    public static final MessageNameFormat.Function FCT_DATE_PARSE = new DateParseFunction();

}
