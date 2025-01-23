package net.thevpc.nuts.installer.panels;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import net.thevpc.nuts.boot.swing.WizardPageBase;
import net.thevpc.nuts.boot.swing.Wizard;
import net.thevpc.nuts.installer.model.ButtonInfo;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.boot.swing.UIHelper;
import net.thevpc.nuts.installer.util.UiHelper2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class DarkModePanel extends WizardPageBase {
    ButtonGroup bg;
    JPanel panel;
    JEditorPane jep;
    JToggleButton lightModeButton;
    JToggleButton darkModeButton;

    public DarkModePanel() {
        super(new BorderLayout());
        add(UIHelper.titleLabel("Please select the installer look"), BorderLayout.PAGE_START);
        GridLayout gg = new GridLayout(1, 2);
        gg.setVgap(10);
        gg.setHgap(10);
        panel = new JPanel(gg);
        int w = 160;
        panel.setPreferredSize(new Dimension(2 * w, w));
        panel.setMaximumSize(new Dimension(2 * w, w));
        bg = new ButtonGroup();
        lightModeButton = add2(new ButtonInfo("Light Mode", "<html><body>Selected <strong>light mode</strong></body></html>", new Color(251, 251, 240), Color.GREEN, UiHelper2.getCheckedImageIcon(false), UiHelper2.getCheckedImageIcon(true)));
        darkModeButton = add2(new ButtonInfo("Dark Mode", "<html><body>Selected <strong>dark mode</strong></body></html>", new Color(35, 37, 38), Color.ORANGE, UiHelper2.getCheckedImageIcon(false), UiHelper2.getCheckedImageIcon(true)));
        darkModeButton.setForeground(Color.WHITE);
        JPanel jp = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        jp.add(panel, c);
        add(jp, BorderLayout.CENTER);
        jep = new JEditorPane();
        jep.setContentType("text/html");
        jep.setEditable(false);

        JScrollPane jsp = new JScrollPane(jep);
        jsp.setMinimumSize(new Dimension(100, 100));
        jsp.setMaximumSize(new Dimension(1000, 100));
        jsp.setPreferredSize(new Dimension(100, 100));
        add(UIHelper.margins(jsp, 10), BorderLayout.PAGE_END);
        jep.setText("<html><body>Selected <strong>light mode</strong></body></html>");
    }

    private JToggleButton add2(ButtonInfo s) {
        JToggleButton a = new JToggleButton(s.text);
        s.bind(a);
//        a.setPreferredSize(new Dimension(60,60));
        panel.add(a);
        bg.add(a);
        a.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                applyPlaf();
            }
        });
        return a;
    }

    private void applyPlaf() {
        InstallData id = InstallData.of(getInstallerContext());
        id.darkMode = darkModeButton.isSelected();

        ButtonInfo ii = ButtonInfo.of(id.darkMode ? darkModeButton : lightModeButton);
        jep.setText(ii.html);
        if (id.darkMode) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(this);
        });
        getInstallerContext().applyPlaf();
        getInstallerContext().setDarkMode(id.darkMode);
    }

    @Override
    public void onAdd(Wizard installerContext, int pageIndex) {
        super.onAdd(installerContext, pageIndex);
        darkModeButton.setSelected(InstallData.of(getInstallerContext()).isDarkMode());
        applyPlaf();
    }

    @Override
    public void onNext() {
        InstallData id = InstallData.of(getInstallerContext());
        id.darkMode = darkModeButton.isSelected();
        super.onNext();
    }

    @Override
    public void onShow() {
        if (!lightModeButton.isSelected() && !darkModeButton.isSelected()) {
            lightModeButton.setSelected(true);
        }
        applyPlaf();
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
    }


}
