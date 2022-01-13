package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.NutsInstaller;
import net.thevpc.nuts.installer.util.UIHelper;
import net.thevpc.nuts.installer.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;

public class PackagesPanel extends AbstractInstallPanel {
    JPanel panel;
    JComponent panelScroll;
    JEditorPane jep;
    int w = 160;
    private final ItemListener buttonInfoItemListener;
    private final MouseAdapter buttonInfoMouseListener;
    private final JScrollPane jsp;

    public PackagesPanel() {
        super(new BorderLayout());
        add(UIHelper.titleLabel("Please select the packages you want to install along with nuts"), BorderLayout.PAGE_START);
        panel = new JPanel(new GridBagLayout());
//        panel.setPreferredSize(new Dimension(2*w,w));
//        panel.setMaximumSize(new Dimension(2*w,w));
        add(panelScroll=UIHelper.margins(new JScrollPane(panel), 10), BorderLayout.CENTER);
        jep = new JEditorPane();
        jep.setContentType("text/html");
        jep.setEditable(false);

        jsp = new JScrollPane(jep);
        jsp.setMinimumSize(new Dimension(400, 200));
        jsp.setMaximumSize(new Dimension(1000, 100));
        jsp.setPreferredSize(new Dimension(100, 100));
        add(jsp, BorderLayout.PAGE_END);
        jep.setText("<html><body>selection the <strong>stable</strong> version for production</body></html>");
        buttonInfoItemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JToggleButton a = (JToggleButton) e.getSource();
                ButtonInfo button = (ButtonInfo) a.getClientProperty("buttonInfo");
                jep.setText(button.desc);
            }
        };
        buttonInfoMouseListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JToggleButton a = (JToggleButton) e.getSource();
                ButtonInfo button = (ButtonInfo) a.getClientProperty("buttonInfo");
                jep.setText(button.desc);
            }
        };
    }

    private void startWaiting() {
        getInstallerContext().startLoading();
    }

    private void endWaiting() {
        getInstallerContext().stopLoading(getPageIndex());
    }

    @Override
    public void onShow() {
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
        new Thread(this::updateButtons).start();
    }

    protected ButtonInfo[] getButtons() {
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            //
//        }
        java.util.List<ButtonInfo> all = new ArrayList<>();
        all.add(new ButtonInfo("<companions>", "Companions", Utils.loadString("package-companions.html", null), false, null, true));
//        all.add(new ButtonInfo("<companions>", "Nuts Store", "Nuts Store", true, null, true));
//        all.add(new ButtonInfo("<companions>", "JEdit", "Text Editor", true, null, false));
//        all.add(new ButtonInfo("<companions>", "Pangaea Note", "Note Taking Application", true, null, false));
        return all.toArray(new ButtonInfo[0]);
    }

    protected void updateButtons() {
        SwingUtilities.invokeLater(() -> {
            startWaiting();
        });
        ButtonInfo[] buttons = getButtons();
        SwingUtilities.invokeLater(() -> {
            endWaiting();
            removeAllComponents2();
            int row = 0;
            int col = 0;
            GridBagConstraints c = new GridBagConstraints();
            for (ButtonInfo button : buttons) {
                JToggleButton a = new JToggleButton(button.name);
                a.setIcon(gelAppUi(button.icon, button.gui));
                a.setSelected(button.selected);
                a.addItemListener(buttonInfoItemListener);
                a.addMouseListener(buttonInfoMouseListener);
                a.putClientProperty("buttonInfo", button);
                a.setMinimumSize(new Dimension(40, 40));
//                a.setPreferredSize(new Dimension(40,40));
                c.fill = GridBagConstraints.BOTH;
                c.insets = new Insets(5, 10, 5, 10);
                c.anchor = GridBagConstraints.CENTER;
                c.weightx = 1;
                c.weighty = 1;
                c.gridx = col;
                c.gridy = row;
                panel.add(a, c);
                col++;
                if (col >= 2) {
                    col = 0;
                    row++;
                }
            }
            panel.invalidate();
            panel.revalidate();
        });
    }

    private void removeAllComponents2() {
        for (Component component : panel.getComponents()) {
            if (component instanceof JToggleButton) {
                ((JToggleButton) component).removeItemListener(buttonInfoItemListener);
                component.removeMouseListener(buttonInfoMouseListener);
            }
        }
        panel.removeAll();
    }

    ImageIcon gelAppUi(String s, boolean gui) {
        try {
            Image image = Toolkit.getDefaultToolkit().getImage(s);
            MediaTracker mt = new MediaTracker(new JLabel());
            mt.addImage(image, 1);
            try {
                mt.waitForAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            if (width > 0 && height > 0) {
                return new ImageIcon(UIHelper.getFixedSizeImage(image, 32, 32));
            }
        } catch (Exception ex) {
            //
        }
        URL r = NutsInstaller.class.getResource(gui ? "mouse.png" : "keyboard.png");
        Image image = Toolkit.getDefaultToolkit().getImage(r);
        return new ImageIcon(UIHelper.getFixedSizeImage(image, 32, 32));
    }

    public class ButtonInfo {
        String id;
        String name;
        String desc;
        String icon;
        boolean gui;
        boolean selected;

        public ButtonInfo(String id, String name, String desc, boolean gui, String icon, boolean selected) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.gui = gui;
            this.icon = icon;
            this.selected = selected;
        }
    }

}
