/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.common;

/**
 *
 * @author vpc
 */
public enum ErrorHandlerIteratorType {
    /**
     * error detected in hasNext will be re-thrown in next (hasNext will return
     * true)
     */
    POSPONE,
    /**
     * error detected in hasNext will be simply thrown
     */
    THROW,
    /**
     * error detected in hasNext will ignored
     */
    IGNORE
}
