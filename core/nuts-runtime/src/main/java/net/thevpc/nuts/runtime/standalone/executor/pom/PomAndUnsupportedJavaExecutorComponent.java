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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.executor.pom;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class PomAndUnsupportedJavaExecutorComponent implements NExecutorComponent {

    public static NId ID;
    NSession session;

    @Override
    public NId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        this.session =context.getSession();
        if(ID==null){
            ID = NId.of("net.thevpc.nuts.exec:java-unsupported").get(session);
        }
        if(true){
            return NConstants.Support.NO_SUPPORT;
        }
        NDefinition def = context.getConstraints(NDefinition.class);
        if (def != null) {
            switch (NStringUtils.trim(def.getDescriptor().getPackaging())){
                case "jar":
                case "war":
                case "zip":
                {
                    return NConstants.Support.NO_SUPPORT;
                }
            }
            return NConstants.Support.DEFAULT_SUPPORT + 1;
        }
        return NConstants.Support.NO_SUPPORT;
    }

    @Override
    public int exec(NExecutionContext executionContext) {
        return execHelper(executionContext).exec();
    }



    public IProcessExecHelper execHelper(NExecutionContext executionContext) {
        return new AbstractSyncIProcessExecHelper(executionContext.getSession()) {

            @Override
            public int exec() {
                throw new NIOException(getSession(), NMsg.ofC("unsupported execution of %s with packaging %s",
                        executionContext.getDefinition().getId(),
                        executionContext.getDefinition().getDescriptor().getPackaging()
                ));
            }
        };
    }
}
