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
public class NutsUnexpectedException extends NutsException {

    public NutsUnexpectedException() {
        this("Unexpected Behaviour");
    }

    public NutsUnexpectedException(String message) {
        super(message);
    }

    public NutsUnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsUnexpectedException(Throwable cause) {
        super(cause);
    }

    public NutsUnexpectedException(IOException cause) {
        super(cause);
    }

    public NutsUnexpectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
