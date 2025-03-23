package net.thevpc.nuts.nswing;

import javax.swing.*;
import java.util.Map;

public interface Wizard {

    void onNextButton();

    void onPreviousButton();

    void onCancelButton();

    void onExitButton();

    void applyPlaf();

    JFrame getFrame();

    JButton getNextButton();

    JButton getPreviousButton();

    JButton getCancelButton();

    JButton getExitButton();

    int getPagesCount();

    boolean hasNext(int pageIndex);

    boolean hasPrevious(int pageIndex);

    Map<String, Object> getVars();


    void startLoading();

    void stopLoading(int index);

    void setDarkMode(boolean darkMode);
}
