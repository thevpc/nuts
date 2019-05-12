/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 */
public interface NutsArgument {

    public boolean isOption();

    public boolean isNonOption();

    public boolean isKeyValue();

    public boolean hasValue();

    public String strKey();

    public String getStrKey();

    public NutsArgument getKey();

    public NutsArgument getName();

    public NutsArgument getValue();

    public String getString();

    public boolean isNull();

    public boolean isBlank();

    public boolean isEmpty();

    public boolean isNegated();

    public boolean isComment();

    public boolean isInt();

    public int getInt();

    public int getInt(int defaultValue);

    public boolean isLong();

    public long getLong();

    public long getLong(long defaultValue);

    public boolean getBoolean();

    public boolean isBoolean();

    public boolean getBoolean(boolean defaultValue);

    public boolean getBooleanValue();

    NutsArgument required();

    String getString(String defaultValue);

}
