package net.thevpc.nuts;

/**
 * The processor is called to process the command line arguments.
 * <ul>
 *  <li>{@code init}: called initially</li>
 *  <li>{@code processOption}|{@code processNonOption}: called multiple times until the command line is consumed</li>
 *  <li>{@code prepare}: called when the command line is fully consumed</li>
 *  <li>{@code exec}|{@code autoComplete}: called to process execution of autcomplete</li>
 * </ul>
 * @category Command Line
 */
public interface NutsCommandLineProcessor {
    /**
     * process the given option argument that was peeked from the command line.
     * Implementations <strong>MUST</strong> call one of
     * the "next" methods to
     * @param option peeked argument
     * @param cmdLine associated commandline
     * @return true if the argument can be processed, false otherwise.
     */
    public boolean nextOption(NutsArgument option, NutsCommandLine cmdLine);

    /**
     * process the given non option argument that was peeked from the command line.
     * Implementations <strong>MUST</strong> call one of
     * the "next" methods to
     * @param nonOption peeked argument
     * @param cmdLine associated commandline
     * @return true if the argument can be processed, false otherwise.
     */
    public boolean nextNonOption(NutsArgument nonOption, NutsCommandLine cmdLine);


    /**
     * initialize the processor
     * @param commandline associated commandline
     */
    public default void init(NutsCommandLine commandline){
        
    }

    /**
     * prepare for execution of for auto-complete
     * @param commandline associated commandline
     */
    public default void prepare(NutsCommandLine commandline){
        
    }

    /**
     * execute options, called after all options was processed and
     * cmdLine.isExecMode() return true.
     */
    public void exec();

    /**
     * called when auto-complete ({@code autoComplete} is not null)
     * @param autoComplete autoComplete instance
     */
    public default void autoComplete(NutsCommandAutoComplete autoComplete){
        
    }

}
