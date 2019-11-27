/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package net.vpc.common.nuts.template;

import java.io.File;
import java.util.List;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public interface ProjectTemplate {

    NutsWorkspace getWorkspace();

    TemplateConsole getConsole();

    void setConfigValue(String propertyName, String value);

    ProjectProperty getConfigProperty(String name);

    List<ProjectTemplateListener> getConfigListeners();

    boolean isAskAll();

    File getProjectRootFolder();

    void setNewlyCreated(String p);

    boolean isNewlyCreated(String p);

}
