package net.thevpc.nuts.spi;

public interface NExecTargetSPI extends NComponent {
    int exec(NExecTargetCommandContext context);
}
