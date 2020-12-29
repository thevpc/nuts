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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.main.executors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreCommonUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.standalone.DefaultNutsExecutionContext;
import net.thevpc.nuts.NutsExecutorComponent;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class JavaSourceNutsExecutorComponent implements NutsExecutorComponent {

    public static final NutsId ID = CoreNutsUtils.parseNutsId("net.thevpc.nuts.exec:exec-java-src");

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> nutsDefinition) {
        if (nutsDefinition != null) {
            if ("java".equals(nutsDefinition.getConstraints().getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public void dryExec(NutsExecutionContext executionContext) throws NutsExecutionException {
        NutsDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        Path javaFile = nutMainFile.getPath();
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        NutsWorkspace ws = executionContext.getWorkspace();
        String folder = "__temp_folder";
        PrintStream out = executionContext.getTraceSession().out();
        out.println("#####compile#####");
        out.printf("```sh embedded-javac -d <temp-folder> %s```%n",javaFile.toString());
        JavaNutsExecutorComponent cc = new JavaNutsExecutorComponent();
        NutsDefinition d = executionContext.getDefinition();
        d = new DefaultNutsDefinition(d);
        ((DefaultNutsDefinition) d).setContent(new NutsDefaultContent(
                folder,
                false,
                true
        ));
        String fileName = javaFile.getFileName().toString();
        NutsExecutionContext executionContext2 = new DefaultNutsExecutionContext(
                d,
                executionContext.getArguments(),
                CoreCommonUtils.concatArrays(
                        executionContext.getExecutorArguments(),
                        new String[]{
                                "--main-class",
                                new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                                "--class-path",
                                folder.toString(),}
                ),
                executionContext.getEnv(),
                executionContext.getExecutorProperties(),
                executionContext.getCwd(),
                executionContext.getTraceSession(),
                executionContext.getExecSession(),
                ws,
                //failFast
                true,
                //temporary
                true,
                executionContext.getExecutionType(),
                executionContext.getCommandName(),
                executionContext.getSleepMillis()
        );
        cc.dryExec(executionContext2);
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        NutsDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        Path javaFile = nutMainFile.getPath();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        NutsWorkspace ws = executionContext.getWorkspace();
        Path folder = Paths.get(ws.io().tmp().createTempFolder("jj", executionContext.getTraceSession()));
        int res = compiler.run(null, null, null, "-d", folder.toString(), javaFile.toString());
        if (res != 0) {
            throw new NutsExecutionException(ws, "compilation failed", res);
        }
        JavaNutsExecutorComponent cc = new JavaNutsExecutorComponent();
        NutsDefinition d = executionContext.getDefinition();
        d = new DefaultNutsDefinition(d);
        ((DefaultNutsDefinition) d).setContent(new NutsDefaultContent(
                folder.toString(),
                false,
                true
        ));
        String fileName = javaFile.getFileName().toString();
        NutsExecutionContext executionContext2 = new DefaultNutsExecutionContext(
                d,
                executionContext.getArguments(),
                CoreCommonUtils.concatArrays(
                        executionContext.getExecutorArguments(),
                        new String[]{
                            "--main-class",
                            new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                            "--class-path",
                            folder.toString()}
                ),
                executionContext.getEnv(),
                executionContext.getExecutorProperties(),
                executionContext.getCwd(),
                executionContext.getTraceSession(),
                executionContext.getExecSession(),
                ws,
                //failFast
                true,
                //temporary
                true,
                executionContext.getExecutionType(),
                executionContext.getCommandName(),
                executionContext.getSleepMillis()
        );
        cc.exec(executionContext2);
    }

}
