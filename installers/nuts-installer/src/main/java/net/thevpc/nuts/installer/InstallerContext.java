package net.thevpc.nuts.installer;

import javax.swing.*;
import java.util.Map;
import java.util.function.Function;

public interface InstallerContext {

    JPanel createBottom();

    void onNextButton();

    void onPreviousButton();

    void onCancelButton();

    void onExitButton();

    JFrame getFrame();

    JButton getNextButton();

    JButton getPreviousButton();

    JButton getCancelButton();

    JButton getExitButton();

    int getPagesCount();

    boolean isInstallFailed();

    NutsInstaller setInstallFailed(boolean installFailed);

    boolean hasNext(int pageIndex);

    boolean hasPrevious(int pageIndex);

    Map<String, Object> getVars();



    public void startLoading();

    public void stopLoading(int index);

    public void setDarkMode(boolean darkMode) ;
}
