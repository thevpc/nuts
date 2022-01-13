/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.template;

import java.util.function.Function;

/**
 *
 * @author thevpc
 */
class IntegerFunction extends AbstractFunction {
    
    public IntegerFunction() {
    }

    @Override
    public Object evalArgs(Object[] args, MessageNameFormat format, Function<String,Object> stringToObject) {
        if (args.length == 0) {
            return null;
        }
        if (args.length == 1) {
            return args[0];
        }
        Object instance = args[0];
        if (!(instance instanceof Number)) {
            instance = Double.parseDouble(instance.toString());
        }
        return ((Number) instance).intValue();
    }
    
}
