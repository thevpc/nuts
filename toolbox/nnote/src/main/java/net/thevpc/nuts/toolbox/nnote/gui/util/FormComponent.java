/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import java.util.List;
import net.thevpc.echo.Application;

/**
 *
 * @author vpc
 */
public interface FormComponent {

    public void install(Application app);

    default void setSelectValues(List<String> values) {
    }

    void setFormChangeListener(Runnable callback);

    void uninstall();

    String getContentString();

    void setContentString(String s);
    
    void setEditable(boolean b);
    
    boolean isEditable();

}
