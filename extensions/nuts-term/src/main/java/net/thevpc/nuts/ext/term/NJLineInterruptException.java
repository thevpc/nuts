/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.ext.term;

/**
 *
 * @author thevpc
 */
public class NJLineInterruptException extends RuntimeException{

    public NJLineInterruptException() {
    }

    public NJLineInterruptException(String string) {
        super(string);
    }
    
}
