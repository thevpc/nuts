/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.toolbox.nadmin.optional.oswindows;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 *
 * @author vpc
 */
public class WinShellHelper {

    private String target;
    private String workingDir;
    private int iconIndex;
    private String path;

    public String getTarget() {
        return target;
    }

    public WinShellHelper setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public WinShellHelper setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    public int getIconIndex() {
        return iconIndex;
    }

    public WinShellHelper setIconIndex(int iconIdex) {
        this.iconIndex = iconIdex;
        return this;
    }

    public String getPath() {
        return path;
    }

    public WinShellHelper setPath(String path) {
        this.path = path;
        return this;
    }

    public void build() {
        mslinks.ShellLink se = mslinks.ShellLink.createLink(getTarget())
                .setWorkingDir(workingDir)
                .setIconLocation("%SystemRoot%\\system32\\SHELL32.dll");
        se.getHeader().setIconIndex(iconIndex);
        se.getConsoleData()
                .setFont(mslinks.extra.ConsoleData.Font.Consolas);
        try {
            //.setFontSize(16)
            //.setTextColor(5)
            se.saveTo(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
