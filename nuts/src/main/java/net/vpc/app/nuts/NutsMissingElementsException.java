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
public class NutsMissingElementsException extends NutsException{

    public NutsMissingElementsException() {
        this("Missing Element");
    }

    public NutsMissingElementsException(String message) {
        super(message);
    }

    public NutsMissingElementsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsMissingElementsException(Throwable cause) {
        super(cause);
    }

    public NutsMissingElementsException(IOException cause) {
        super(cause);
    }

    public NutsMissingElementsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
