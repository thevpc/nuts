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
package net.thevpc.nuts.runtime.standalone.executors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDefinition;
import net.thevpc.nuts.NutsExecutorComponent;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.util.CoreArrayUtils;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class JavaSourceExecutorComponent implements NutsExecutorComponent {

    public static NutsId ID;
    NutsWorkspace ws;

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> nutsDefinition) {
        this.ws = nutsDefinition.getWorkspace();
        if (ID == null) {
            ID = ws.id().parser().parse("net.thevpc.nuts.exec:exec-java-src");
        }
        if ("java".equals(nutsDefinition.getConstraints().getDescriptor().getPackaging())) {
            return DEFAULT_SUPPORT + 1;
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
        out.println(executionContext.getWorkspace().text().forStyled("compile", NutsTextStyle.primary(4)));
        out.printf("%s%n",
                executionContext.getWorkspace().commandLine().create(
                        "embedded-javac",
                        "-d",
                        "<temp-folder>",
                        javaFile.toString()
                )
        );
        JavaExecutorComponent cc = new JavaExecutorComponent();
        NutsDefinition d = executionContext.getDefinition();
        d = new DefaultNutsDefinition(d, executionContext.getTraceSession());
        ((DefaultNutsDefinition) d).setContent(new NutsDefaultContent(
                folder,
                false,
                true
        ));
        String fileName = javaFile.getFileName().toString();
        NutsExecutionContext executionContext2 = NutsWorkspaceExt.of(executionContext.getWorkspace())
                .createExecutionContext()
                .setAll(executionContext)
                .setDefinition(d)
                .setExecutorArguments(CoreArrayUtils.concatArrays(
                        executionContext.getExecutorArguments(),
                        new String[]{
                            "--main-class",
                            new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                            "--class-path",
                            folder.toString()
                        }
                ))
                .setFailFast(true)
                .setTemporary(true)
                .build();
        cc.dryExec(executionContext2);
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        NutsDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        Path javaFile = nutMainFile.getPath();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        NutsWorkspace ws = executionContext.getWorkspace();
        Path folder = Paths.get(ws.io().tmp()
                .setSession(executionContext.getTraceSession())
                .createTempFolder("jj"));
        int res = compiler.run(null, null, null, "-d", folder.toString(), javaFile.toString());
        if (res != 0) {
            throw new NutsExecutionException(executionContext.getTraceSession(), "compilation failed", res);
        }
        JavaExecutorComponent cc = new JavaExecutorComponent();
        NutsDefinition d = executionContext.getDefinition();
        d = new DefaultNutsDefinition(d, executionContext.getTraceSession());
        ((DefaultNutsDefinition) d).setContent(new NutsDefaultContent(
                folder.toString(),
                false,
                true
        ));
        String fileName = javaFile.getFileName().toString();
        NutsExecutionContext executionContext2 = NutsWorkspaceExt.of(executionContext.getWorkspace())
                .createExecutionContext()
                .setAll(executionContext)
                .setDefinition(d)
                .setExecutorArguments(CoreArrayUtils.concatArrays(
                        executionContext.getExecutorArguments(),
                        new String[]{
                            "--main-class",
                            new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                            "--class-path",
                            folder.toString()}
                ))
                .setFailFast(true)
                .setTemporary(true)
                .build();
        cc.exec(executionContext2);
    }

}
