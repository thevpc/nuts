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
public enum NIteratorErrorHandlerType implements NEnum{
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
    IGNORE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NIteratorErrorHandlerType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NIteratorErrorHandlerType> parse(String value) {
        return NEnumUtils.parseEnum(value, NIteratorErrorHandlerType.class);
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
