package net.vpc.app.nuts;

public interface NutsTerminalManager {

    /**
     * return terminal format that handles metrics and format/escape methods
     *
     * @return terminal format that handles metrics and format/escape methods
     */
    NutsTerminalFormat getTerminalFormat();


    /**
     * return terminal format that handles metrics and format/escape methods.
     *
     * @return terminal format that handles metrics and format/escape methods
     */
    NutsSystemTerminal systemTerminal();

    /**
     * return workspace system terminal.
     *
     * @return workspace system terminal
     */
    NutsSystemTerminal getSystemTerminal();

    NutsTerminalManager enableRichTerm(NutsSession session);

    NutsSystemTerminal createSystemTerminal(NutsTerminalSpec spec);

    /**
     * update workspace wide system terminal
     *
     * @param terminal system terminal
     * @return {@code this} instance
     */
    NutsTerminalManager setSystemTerminal(NutsSystemTerminalBase terminal,NutsSession session);

    /**
     * return workspace default terminal
     *
     * @return workspace default terminal
     */
    NutsSessionTerminal terminal();

    /**
     * return workspace default terminal
     *
     * @return workspace default terminal
     */
    NutsSessionTerminal getTerminal();

    /**
     * update workspace wide terminal
     *
     * @param terminal terminal
     * @param session
     * @return {@code this} instance
     */
    NutsTerminalManager setTerminal(NutsSessionTerminal terminal, NutsSession session);

    /**
     * return new terminal bound to system terminal
     *
     * @return new terminal
     * @param session
     */
    NutsSessionTerminal createTerminal(NutsSession session);


    /**
     * return new terminal bound to the given {@code parent}
     *
     * @param parent parent terminal or null
     * @param session
     * @return new terminal
     */
    NutsSessionTerminal createTerminal(NutsTerminalBase parent, NutsSession session);
}
