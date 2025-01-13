/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.deprecated;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

/**
 *
 * @author thevpc
 */
class DateFunction extends AbstractFunction {
    
    public DateFunction() {
    }

    @Override
    public Object evalArgs(Object[] args, MessageNameFormat format, Function<String,Object> stringToObject) {
        if (args.length == 0) {
            return null;
        }
        if (args.length == 1) {
            Locale loc = Locale.getDefault();
            return DateFormat.getDateInstance(DateFormat.DEFAULT, loc).format((Date) args[0]);
        }
        Object instance = args[0];
        Locale loc = Locale.getDefault();
        if (args.length >= 3) {
            loc = new Locale(String.valueOf(args[2]));
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("short")) {
            return DateFormat.getDateInstance(DateFormat.SHORT, loc).format((Date) instance);
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("medium")) {
            return DateFormat.getDateInstance(DateFormat.DEFAULT, loc).format((Date) instance);
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("long")) {
            return DateFormat.getDateInstance(DateFormat.LONG, loc).format((Date) instance);
        }
        if (String.valueOf(args[1]).equalsIgnoreCase("full")) {
            return DateFormat.getDateInstance(DateFormat.FULL, loc).format((Date) instance);
        }
        return new SimpleDateFormat(String.valueOf(args[1]), loc).format((Date) instance);
    }
    
}
