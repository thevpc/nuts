/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NApiUtilsRPI;

/**
 * Nuts App. Nuts is a Package manager for Java Applications and this
 * class is its main class for creating and opening nuts workspaces.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.1.0
 */
public final class NutsApp {


    /**
     * main method. This Main will call
     * {@link Nuts#runWorkspace(java.lang.String...)} then
     * {@link System#exit(int)} at completion
     *
     * @param args main arguments
     */
    @SuppressWarnings("UseSpecificCatch")
    public static void main(String[] args) throws Throwable {
        try {
            Nuts.runWorkspace(args);
            System.exit(0);
        } catch (Exception ex) {
            NSession session = NSessionAwareExceptionBase.resolveSession(ex).orNull();
            if (session != null) {
                System.exit(NApplicationExceptionHandler.of()
                        .processThrowable(args, ex));
            } else {
                System.exit(NApiUtilsRPI.processThrowable(ex, args));
            }
        }
    }
}