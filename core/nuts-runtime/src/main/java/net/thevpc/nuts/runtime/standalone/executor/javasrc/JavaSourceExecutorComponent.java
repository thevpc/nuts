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
package net.thevpc.nuts.runtime.standalone.executor.javasrc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsTmp;
import net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorComponent;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsDefinition;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsExecutorComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class JavaSourceExecutorComponent implements NutsExecutorComponent {

    public static NutsId ID;
    NutsSession session;

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        NutsDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        Path javaFile = nutMainFile.getContent().map(NutsPath::toFile).orNull();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        NutsSession session = executionContext.getSession();
        Path folder = NutsTmp.of(session)
                .createTempFolder("jj").toFile();
        int res = compiler.run(null, null, null, "-d", folder.toString(), javaFile.toString());
        if (res != 0) {
            throw new NutsExecutionException(session, NutsMessage.cstyle("compilation failed"), res);
        }
        JavaExecutorComponent cc = new JavaExecutorComponent();
        NutsDefinition d = executionContext.getDefinition();
        d = new DefaultNutsDefinition(d, session);
        ((DefaultNutsDefinition) d).setContent(NutsPath.of(folder, session).setUserCache(false).setUserTemporary(true));
        String fileName = javaFile.getFileName().toString();
        List<String> z = new ArrayList<>(executionContext.getExecutorOptions());
        z.addAll(Arrays.asList("--main-class",
                new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                "--class-path",
                folder.toString()));
        NutsExecutionContext executionContext2 = NutsWorkspaceExt.of(executionContext.getSession())
                .createExecutionContext()
                .setAll(executionContext)
                .setDefinition(d)
                .setExecutorOptions(z)
                .setFailFast(true)
                .setTemporary(true)
                .build();
        cc.exec(executionContext2);
    }

    @Override
    public void dryExec(NutsExecutionContext executionContext) throws NutsExecutionException {
        NutsDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        Path javaFile = nutMainFile.getContent().map(NutsPath::toFile).orNull();
        String folder = "__temp_folder";
        NutsPrintStream out = executionContext.getSession().out();
        out.println(NutsTexts.of(executionContext.getSession()).ofStyled("compile", NutsTextStyle.primary4()));
        out.printf("%s%n",
                NutsCommandLine.of(
                        new String[]{
                                "embedded-javac",
                                "-d",
                                "<temp-folder>",
                                javaFile.toString()
                        }
                )
        );
        JavaExecutorComponent cc = new JavaExecutorComponent();
        NutsDefinition d = executionContext.getDefinition();
        d = new DefaultNutsDefinition(d, executionContext.getSession());
        ((DefaultNutsDefinition) d).setContent(
                NutsPath.of(folder, executionContext.getSession()).setUserCache(false).setUserTemporary(true)
        );
        String fileName = javaFile.getFileName().toString();
        List<String> z = new ArrayList<>(executionContext.getExecutorOptions());
        z.addAll(Arrays.asList("--main-class",
                new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                "--class-path",
                folder.toString()));
        NutsExecutionContext executionContext2 = NutsWorkspaceExt.of(executionContext.getSession())
                .createExecutionContext()
                .setAll(executionContext)
                .setDefinition(d)
                .setExecutorOptions(z)
                .setFailFast(true)
                .setTemporary(true)
                .build();
        cc.dryExec(executionContext2);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        this.session = context.getSession();
        if (ID == null) {
            ID = NutsId.of("net.thevpc.nuts.exec:exec-java-src").get(session);
        }
        NutsDefinition def = context.getConstraints(NutsDefinition.class);
        if (def != null) {
            if ("java".equals(def.getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

}
