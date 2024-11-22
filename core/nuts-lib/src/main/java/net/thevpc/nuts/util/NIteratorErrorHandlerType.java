/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.util;

/**
 *
 * @author thevpc
 */
public enum NIteratorErrorHandlerType {
    /**
     * error detected in hasNext will be re-thrown in next (hasNext will return
     * true)
     */
    POSTPONE,
    /**
     * error detected in hasNext will be simply thrown
     */
    THROW,
    /**
     * error detected in hasNext will ignored
     */
    IGNORE
}
