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
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorComponent;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

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
@NComponentScope(NComponentScopeType.WORKSPACE)
public class JavaSourceExecutorComponent implements NExecutorComponent {

    public static NId ID;
    NSession session;

    @Override
    public NId getId() {
        return ID;
    }

    @Override
    public void exec(NExecutionContext executionContext) {
        if(executionContext.getExecSession().isDry()){
            NDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
            Path javaFile = nutMainFile.getContent().map(NPath::toFile).orNull();
            String folder = "__temp_folder";
            NPrintStream out = executionContext.getSession().out();
            out.println(NTexts.of(executionContext.getSession()).ofStyled("compile", NTextStyle.primary4()));
            out.println(
                    NCmdLine.of(
                            new String[]{
                                    "embedded-javac",
                                    "-d",
                                    "<temp-folder>",
                                    javaFile.toString()
                            }
                    )
            );
            JavaExecutorComponent cc = new JavaExecutorComponent();
            NDefinition d = executionContext.getDefinition();
            d = new DefaultNDefinition(d, executionContext.getSession());
            ((DefaultNDefinition) d).setContent(
                    NPath.of(folder, executionContext.getSession()).setUserCache(false).setUserTemporary(true)
            );
            String fileName = javaFile.getFileName().toString();
            List<String> z = new ArrayList<>(executionContext.getExecutorOptions());
            z.addAll(Arrays.asList("--main-class",
                    new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                    "--class-path",
                    folder.toString()));
            NExecutionContext executionContext2 = NWorkspaceExt.of(executionContext.getSession())
                    .createExecutionContext()
                    .setAll(executionContext)
                    .setDefinition(d)
                    .setExecutorOptions(z)
                    .setFailFast(true)
                    .setTemporary(true)
                    .build();
            cc.exec(executionContext2);
        }else {
            NDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
            Path javaFile = nutMainFile.getContent().map(NPath::toFile).orNull();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            NSession session = executionContext.getSession();
            Path folder = NPaths.of(session)
                    .createTempFolder("jj").toFile();
            int res = compiler.run(null, null, null, "-d", folder.toString(), javaFile.toString());
            if (res != 0) {
                throw new NExecutionException(session, NMsg.ofPlain("compilation failed"), res);
            }
            JavaExecutorComponent cc = new JavaExecutorComponent();
            NDefinition d = executionContext.getDefinition();
            d = new DefaultNDefinition(d, session);
            ((DefaultNDefinition) d).setContent(NPath.of(folder, session).setUserCache(false).setUserTemporary(true));
            String fileName = javaFile.getFileName().toString();
            List<String> z = new ArrayList<>(executionContext.getExecutorOptions());
            z.addAll(Arrays.asList("--main-class",
                    new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                    "--class-path",
                    folder.toString()));
            NExecutionContext executionContext2 = NWorkspaceExt.of(executionContext.getSession())
                    .createExecutionContext()
                    .setAll(executionContext)
                    .setDefinition(d)
                    .setExecutorOptions(z)
                    .setFailFast(true)
                    .setTemporary(true)
                    .build();
            cc.exec(executionContext2);
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        this.session = context.getSession();
        if (ID == null) {
            ID = NId.of("net.thevpc.nuts.exec:exec-java-src").get(session);
        }
        NDefinition def = context.getConstraints(NDefinition.class);
        if (def != null) {
            if ("java".equals(def.getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

}
