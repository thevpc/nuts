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
package net.thevpc.nuts.core.test.whitebox.utilities;

import java.io.File;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.bundles.mvn.Pom;
import net.thevpc.nuts.runtime.bundles.mvn.PomLogger;
import net.thevpc.nuts.runtime.bundles.mvn.PomXmlParser;

/**
 *
 * @author vpc
 */
public class TestPomXmlParser {
    public static void main(String[] args) {
        String path="/data/git/dbclient/modules/dbclient-plugins/tool/tool-neormf/pom.xml";
        NutsWorkspace ws = Nuts.openWorkspace("-k","-y");
        PomXmlParser p=new PomXmlParser(PomLogger.DEFAULT);
        try {
            Pom t = p.parse(new File(path), ws.createSession());
            System.out.println(t);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
