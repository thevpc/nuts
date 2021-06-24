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
public abstract class AbstractFunction implements MessageNameFormat.Function {
    
    @Override
    public final Object eval(MessageNameFormat.ExprNode[] args, MessageNameFormat format, Function<String,Object> provider,
                             MessageNameFormatContext messageNameFormatContext) {
        Object[] oargs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            oargs[i] = args[i].format(format, provider, messageNameFormatContext);
        }
        return evalArgs(oargs, format, provider);
    }

    public abstract Object evalArgs(Object[] args, MessageNameFormat format, Function<String,Object> provider);
    
}
