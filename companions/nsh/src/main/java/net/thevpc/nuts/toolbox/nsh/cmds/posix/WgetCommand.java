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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds.posix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class WgetCommand extends NShellBuiltinDefault {

    public WgetCommand() {
        super("wget", NConstants.Support.DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (cmdLine.next("-O", "--output-document").isPresent()) {
            options.outputDocument = cmdLine.nextNonOption().get().asString().orNull();
            return true;
        } else if (!cmdLine.isNextOption()) {
            while (cmdLine.hasNext()) {
                options.files.add(cmdLine.next().flatMap(NLiteral::asString).get());
            }
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            throw new NExecutionException(NMsg.ofPlain("wget: Missing Files"), NExecutionException.ERROR_2);
        }
        for (String file : options.files) {
            download(file, options.outputDocument, context);
        }
    }

    protected void download(String path, String output, NShellExecutionContext context) {
        String output2 = output;
        NSession session = context.getSession();
        String urlName = NPath.of(path).getName();
        if (!NBlankable.isBlank(output2)) {
            output2 = output2.replace("{}", urlName);
        }
        NPath file = NPath.of(context.getAbsolutePath(NBlankable.isBlank(output2) ? urlName : output2));
        NCp.of()
                .from(NPath.of(path)).to(file)
                .addOptions(NPathOption.LOG, NPathOption.TRACE).run();
    }

    private static class Options {

        String outputDocument = null;
        List<String> files = new ArrayList<>();
    }
    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
