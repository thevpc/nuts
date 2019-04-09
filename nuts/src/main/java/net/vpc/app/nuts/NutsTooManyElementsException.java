/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.IOException;

/**
 *
 * @author vpc
 */
public class NutsTooManyElementsException extends NutsException{

    public NutsTooManyElementsException() {
        this("Too many Elements");
    }

    public NutsTooManyElementsException(String message) {
        super(message);
    }

    public NutsTooManyElementsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsTooManyElementsException(Throwable cause) {
        super(cause);
    }

    public NutsTooManyElementsException(IOException cause) {
        super(cause);
    }

    public NutsTooManyElementsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
