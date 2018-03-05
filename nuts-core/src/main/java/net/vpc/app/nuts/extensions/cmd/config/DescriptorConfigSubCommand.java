/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.cmd.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.extensions.cmd.AbstractConfigSubCommand;
import net.vpc.app.nuts.extensions.cmd.ConfigCommand;
import net.vpc.app.nuts.extensions.cmd.cmdline.FileNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.ValueNonOption;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.Ref;
import net.vpc.common.commandline.CommandLine;

/**
 *
 * @author vpc
 */
public class DescriptorConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        boolean newDesc = false;
        String file = null;
        boolean save = false;
        final Ref<NutsDescriptor> desc = new Ref<>();
        if (cmdLine.read("new descriptor", "nd")) {
            newDesc = true;
        } else if (cmdLine.read("update descriptor", "ud")) {
            newDesc = false;
        } else {
            return false;
        }

        List<Runnable> all = new ArrayList<>();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.read("-executable")) {
                final boolean value = cmdLine.readNonOptionOrError(new ValueNonOption("executable", null, "true", "false")).getBoolean();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setExecutable(value));
                    }
                });
            } else if (cmdLine.read("-ext")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("ext", null, "jar")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setExt(value));
                    }
                });
            } else if (cmdLine.read("-packaging")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("packaging", null, "jar")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setPackaging(value));
                    }
                });
            } else if (cmdLine.read("-name")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("name", null, "my-name")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setId(desc.get().getId().setName(value)));
                    }
                });
            } else if (cmdLine.read("-group")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("group", null, "my-group")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setId(desc.get().getId().setGroup(value)));
                    }
                });
            } else if (cmdLine.read("-id")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("id", null, "my-group:my-name#1.0")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setId(context.getValidWorkspace().getExtensionManager().getFactory().parseNutsId(value)));
                    }
                });

            } else if (cmdLine.read("-add-os")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().addOs(value));
                    }
                });
            } else if (cmdLine.read("-remove-os")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removeOs(value));
                    }
                });

            } else if (cmdLine.read("-add-osdist")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().addOsdist(value));
                    }
                });
            } else if (cmdLine.read("-remove-osdist")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removeOsdist(value));
                    }
                });

            } else if (cmdLine.read("-add-platform")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().addPlatform(value));
                    }
                });
            } else if (cmdLine.read("-remove-platform")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removePlatform(value));
                    }
                });

            } else if (cmdLine.read("-add-arch")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().addArch(value));
                    }
                });
            } else if (cmdLine.read("-remove-arch")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removeArch(value));
                    }
                });
            } else if (cmdLine.read("-add-property")) {
                String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                final String[] nv = CoreNutsUtils.splitNameAndValue(value);
                if (nv != null) {
                    all.add(new Runnable() {
                        @Override
                        public void run() {
                            desc.set(desc.get().addProperty(nv[0], nv[1]));
                        }
                    });
                }
            } else if (cmdLine.read("-remove-property")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removeProperty(value));
                    }
                });

            } else if (cmdLine.read("-add-dependency")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("dependency", null, "my-group:my-name#1.0")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().addDependency(CoreNutsUtils.parseNutsDependency(value)));
                    }
                });
            } else if (cmdLine.read("-remove-dependency")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("dependency", null, "my-group:my-name#1.0")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removeDependency(CoreNutsUtils.parseNutsDependency(value)));
                    }
                });
            } else if (cmdLine.read("-file")) {
                file = cmdLine.readNonOptionOrError(new FileNonOption("file")).getString();
            } else if (cmdLine.read("-save")) {
                save = cmdLine.readNonOptionOrError(new ValueNonOption("save", null, "true", "false")).getBoolean();
            } else {
                if (!cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentException("Unsupported");
                }
            }
        }
        if (cmdLine.isExecMode()) {
            if (newDesc) {
                desc.set(CoreNutsUtils.createNutsDescriptor());
            } else {
                if (file != null) {
                    desc.set(CoreNutsUtils.parseNutsDescriptor(new File(file)));
                } else {
                    if (cmdLine.isExecMode()) {
                        throw new NutsIllegalArgumentException("-file missing");
                    }
                }
            }

            for (Runnable r : all) {
                r.run();
            }
            if (save) {
                if (file != null) {
                    desc.get().write(new File(file));
                } else {
                    if (cmdLine.isExecMode()) {
                        throw new NutsIllegalArgumentException("-file missing");
                    }
                }
            } else {
                context.getTerminal().getOut().printf("%s\n", desc.get().toString(true));
            }
        }
        return true;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT;
    }

}
