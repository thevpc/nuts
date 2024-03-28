/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.updatestats;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCmdBase;
import net.thevpc.nuts.spi.NUpdateRepositoryStatsCmd;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNUpdateRepositoryStatsCmd extends NRepositoryCmdBase<NUpdateRepositoryStatsCmd>
        implements NUpdateRepositoryStatsCmd {

    protected NRepository repo;

    public AbstractNUpdateRepositoryStatsCmd(NRepository repo) {
        super(repo, "update-repo-statistics");
        this.repo = repo;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().orNull();
        if (a == null) {
            return false;
        }
        switch(a.key()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

}
