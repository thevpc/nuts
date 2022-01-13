/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package net.thevpc.nuts.lib.template;

import java.io.File;
import java.util.List;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author thevpc
 */
public interface ProjectTemplate {

    NutsSession getSession();

    TemplateConsole getConsole();

    void setConfigValue(String propertyName, String value);

    ProjectProperty getConfigProperty(String name);

    List<ProjectTemplateListener> getConfigListeners();

    boolean isAskAll();

    File getProjectRootFolder();

    void setNewlyCreated(String p);

    boolean isNewlyCreated(String p);

}
