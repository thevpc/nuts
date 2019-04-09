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
public class NutsUnsatisfiedRequirementsExeption extends NutsException{

    public NutsUnsatisfiedRequirementsExeption() {
    }

    public NutsUnsatisfiedRequirementsExeption(String message) {
        super(message);
    }

    public NutsUnsatisfiedRequirementsExeption(String message, Throwable cause) {
        super(message, cause);
    }

    public NutsUnsatisfiedRequirementsExeption(Throwable cause) {
        super(cause);
    }

    public NutsUnsatisfiedRequirementsExeption(IOException cause) {
        super(cause);
    }

    public NutsUnsatisfiedRequirementsExeption(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
