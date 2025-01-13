/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.deprecated;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thevpc
 */
class DateParseFunction extends AbstractFunction {
    
    public DateParseFunction() {
    }

    @Override
    public Object evalArgs(Object[] args, MessageNameFormat format, Function<String,Object> stringToObject) {
        if (args.length == 0) {
            return null;
        }
        Object instance = args[0];
        Locale loc = (args.length >= 3) ? new Locale(String.valueOf(args[2])) : Locale.getDefault();
        for (int i = 2; i < args.length; i++) {
            DateFormat dateFormat = MessageNameFormat.resolveDateFormat(String.valueOf(args[i]), loc, "yyyy-MM-dd");
            try {
                return dateFormat.parse(String.valueOf(instance));
            } catch (ParseException ex) {
                Logger.getLogger(MessageNameFormat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        DateFormat dateFormat = MessageNameFormat.resolveDateFormat(null, loc, "yyyy-MM-dd");
        try {
            return dateFormat.parse(String.valueOf(instance));
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
}
