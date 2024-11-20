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
package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

import net.thevpc.nuts.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.cmds.util.*;
import net.thevpc.nuts.toolbox.nsh.cmds.util.filter.JavaExceptionWindowFilter;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.FileInfo;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

/**
 * Created by vpc on 1/7/17.
 */
public class GrepCommand extends NShellBuiltinDefault {

    public GrepCommand() {
        super("grep", NConstants.Support.DEFAULT_SUPPORT, GrepOptions.class);
    }


    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        NSession session = context.getSession();
        GrepOptions options = context.getOptions();
        if (!options.withNutsOptions && options.expressions.isEmpty()) {
            options.expressions.add(
                    new ExpressionInfo()
                            .setPattern(cmdLine.next().flatMap(NLiteral::asString).get())
                            .setIgnoreCase(options.ignoreCase)
                            .setInvertMatch(options.invertMatch)
                            .setWord(options.word)
            );
        } else {
            String path = cmdLine.next().flatMap(NLiteral::asString).get();
            options.files.add(new FileInfo(NPath.of(path), options.highlighter));
        }
        return true;
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        NSession session = context.getSession();
        GrepOptions options = context.getOptions();
        NArg a;
        if (cmdLine.next("-").orNull() != null) {
            options.files.add(null);
            return true;
        } else if ((a = cmdLine.next("-e", "--regexp").orNull()) != null) {
            //options.regexp = true;
            return true;
        } else if ((a = cmdLine.next("-v", "--invert-match").orNull()) != null) {
            if (a.isActive()) {
                if (a.isNegated()) {
                    String v = a.getStringValue().orNull();
                    if (v == null) {
                        options.invertMatch = false;
                    } else {
                        options.expressions.add(
                                new ExpressionInfo()
                                        .setPattern(a.getStringValue().get())
                                        .setIgnoreCase(options.ignoreCase)
                                        .setInvertMatch(false)
                                        .setWord(options.word)
                        );
                    }
                } else {
                    String v = a.getStringValue().orNull();
                    if (v == null) {
                        options.invertMatch = true;
                    } else {
                        options.expressions.add(
                                new ExpressionInfo()
                                        .setPattern(a.getStringValue().get())
                                        .setIgnoreCase(options.ignoreCase)
                                        .setInvertMatch(true)
                                        .setWord(options.word)
                        );
                    }
                }
            }
            return true;
        } else if ((a = cmdLine.nextEntry("-f", "--file").orNull()) != null) {
            try {
                Files.lines(Paths.get(a.getStringValue().get()))
                        .forEach(line -> {
                            line = line.trim();
                            if (line.length() > 0) {
                                options.expressions.add(
                                        new ExpressionInfo()
                                                .setPattern(line)
                                                .setIgnoreCase(options.ignoreCase)
                                                .setInvertMatch(options.invertMatch)
                                                .setWord(options.word)
                                );
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        } else if (cmdLine.next("-w", "--word-regexp").orNull() != null) {
            options.word = true;
            return true;
        } else if (cmdLine.next("-x", "--line-regexp").orNull() != null) {
            options.lineRegexp = true;
            return true;
        } else if (cmdLine.next("-i", "--ignore-case").orNull() != null) {
            options.ignoreCase = true;
            return true;
        } else if (cmdLine.next("-r", "--recursive").orNull() != null) {
            options.recursive = true;
            options.followSymbolicLinks = false;
            return true;
        } else if (cmdLine.next("-R", "--dereference-recursive").orNull() != null) {
            options.recursive = true;
            options.followSymbolicLinks = true;
            return true;
        } else if (parseNutsSpecific(cmdLine, options,session)) {
            return true;
        } else if (cmdLine.next("-n").isPresent()) {
            options.n = true;
            return true;
        } else {
            return false;
        }
    }

    private static void processRequireNutsOption(NArg a, NCmdLine cmdLine, GrepOptions options) {
        if (!options.withNutsOptions) {
            if (options.requireNutsOptions) {
                cmdLine.throwUnexpectedArgument(NMsg.ofC("option can be used along with --nuts", a));
            } else {
                options.withNutsOptions = true;
            }
        }
    }

    private boolean parseNutsSpecific(NCmdLine cmdLine, GrepOptions options,NSession session) {
        NArg a;
        if ((a = cmdLine.next("--nuts").orNull()) != null) {
            options.withNutsOptions = true;
            return true;
        }else if ((a = cmdLine.nextEntry("--expr", "--like").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.expressions.add(
                    new ExpressionInfo()
                            .setPattern(a.getStringValue().get())
                            .setIgnoreCase(options.ignoreCase)
                            .setInvertMatch(options.invertMatch)
                            .setWord(options.word)
            );
            return true;
        } else if ((a = cmdLine.nextEntry("--file-name").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.fileNames.add(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.nextEntry("--file-iname").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.fileNamesIgnoreCase.add(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.next("--from").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.from = NLiteral.of(a).asLong().orElse(null);
            return true;
        } else if ((a = cmdLine.next("--to").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.to = NLiteral.of(a).asLong().orElse(null);
            return true;
        } else if ((a = cmdLine.next("--@include").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            for (String s : NPath.of(NLiteral.of(a).asString().get()).getLines().collect(Collectors.toList())) {
                s = s.trim();
                if (!s.isEmpty()) {
                    if (!s.startsWith("#")) {
                        String[] found = NCmdLine.parse(s).get().toStringArray();
                        cmdLine.pushBack(found);
                    }
                }
            }
            return true;
        } else if ((a = cmdLine.next("--range").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            NumberRangeList rl = NumberRangeList.parse(a.getStringValue().get());
            if (rl != null) {
                NumberRange r = rl.toRange();
                options.from = r.getFrom();
                options.to = r.getTo();
            }
            return true;
        } else if ((a = cmdLine.nextFlag("--summary", "-s").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.summary = a.getBooleanValue().get();
            return true;
        } else if ((a = cmdLine.next("--less").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.byLine = true;
            return true;
        } else if ((a = cmdLine.next("-H", "--highlight", "--highlighter").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.highlighter = NStringUtils.trim(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.next("-S", "--selection-style").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.selectionStyle = NStringUtils.trimToNull(a.getStringValue().get());
            return true;
        } else if (parseJex(cmdLine, options)) {
            return true;
        }
        return false;
    }

    private boolean parseJex(NCmdLine cmdLine, GrepOptions options) {
        NArg a;
        if ((a = cmdLine.next("--jex", "--java-exception").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.windowFilter.add(options.lastJavaExceptionWindowFilter = new JavaExceptionWindowFilter());
            return true;
        } else if ((a = cmdLine.nextEntry("--jex-rows").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            if (options.lastJavaExceptionWindowFilter != null) {
                options.lastJavaExceptionWindowFilter.setRows(a.getValue().asInt().get());
            } else {
                cmdLine.throwError(NMsg.ofPlain("expected --jex first"));
            }
            return true;
        } else if ((a = cmdLine.nextEntry("--jex-include").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            if (options.lastJavaExceptionWindowFilter != null) {
                options.lastJavaExceptionWindowFilter.getJexFilters().add(new JavaExceptionWindowFilter.JexFilter(a.getValue().asString().get(), true));
            } else {
                cmdLine.throwError(NMsg.ofPlain("expected --jex first"));
            }
            return true;
        } else if ((a = cmdLine.nextEntry("--jex-exclude").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            if (options.lastJavaExceptionWindowFilter != null) {
                options.lastJavaExceptionWindowFilter.getJexFilters().add(new JavaExceptionWindowFilter.JexFilter(a.getValue().asString().get(), true));
            } else {
                cmdLine.throwError(NMsg.ofPlain("expected --jex first"));
            }
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        GrepOptions options = context.getOptions();
        GrepService service = new GrepService();
        service.run(options, context.getSession());
    }


}
