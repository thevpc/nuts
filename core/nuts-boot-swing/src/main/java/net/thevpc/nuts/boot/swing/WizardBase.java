package net.thevpc.nuts.boot.swing;

import net.thevpc.nuts.boot.swing.GBC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class WizardBase implements Wizard {
    private final Map<String, Object> vars = new HashMap<>();
    private final java.util.List<WizardPageBase> panels = new ArrayList<>();
    private JPanel mainPanel;
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel centerPanel;
    private int currentIndex;
    private JButton nextButton;
    private JButton previousButton;
    private JProgressBar progressBar;
    private JButton cancelButton;
    private JComponent leftComponent;
    private JButton exitButton;
    private String frameTitle;
    private Image frameIconImage;
    private boolean exitOnCloseFrame;

    public boolean isExitOnCloseFrame() {
        return exitOnCloseFrame;
    }

    public void setExitOnCloseFrame(boolean exitOnCloseFrame) {
        this.exitOnCloseFrame = exitOnCloseFrame;
    }

    public String getFrameTitle() {
        return frameTitle;
    }

    public Image getFrameIconImage() {
        return frameIconImage;
    }

    public void setFrameIconImage(Image frameIconImage) {
        this.frameIconImage = frameIconImage;
        if (frameTitle != null) {
            frame.setIconImage(frameIconImage);
        }
    }

    public void setFrameTitle(String frameTitle) {
        this.frameTitle = frameTitle;
        if (frameTitle != null) {
            frame.setTitle(frameTitle);
        }
    }

    @Override
    public void onNextButton() {
        if (hasNext(currentIndex)) {
            panels.get(currentIndex - 1).onNext();
            showPage(currentIndex + 1);
        }
    }

    @Override
    public void onPreviousButton() {
        if (hasPrevious(currentIndex)) {
            panels.get(currentIndex - 1).onPrevious();
            showPage(currentIndex - 1);
        }
    }

    @Override
    public void onCancelButton() {
        panels.get(currentIndex - 1).onCancel();
        frame.dispose();
    }

    @Override
    public void onExitButton() {
        frame.dispose();
    }

    @Override
    public JFrame getFrame() {
        return frame;
    }

    @Override
    public JButton getNextButton() {
        return nextButton;
    }

    @Override
    public JButton getPreviousButton() {
        return previousButton;
    }

    @Override
    public JButton getCancelButton() {
        return cancelButton;
    }

    @Override
    public JButton getExitButton() {
        return exitButton;
    }

    @Override
    public int getPagesCount() {
        return panels.size();
    }

    public void setDarkMode(boolean darkMode) {
        Color cc = darkMode ? new Color(0x3b3e40) : new Color(0xf2f2f2);
        leftComponent.setBackground(cc);
        for (Component component : leftComponent.getComponents()) {
            component.setBackground(cc);
        }
    }

    public void setProgressByPageIndex() {
        progressBar.setIndeterminate(false);
        int n = (int) (currentIndex * 100.0 / (getPagesCount()));
        progressBar.setValue(n);
    }

    @Override
    public void applyPlaf() {
        JFrame f = getFrame();
        if (f != null) {
            SwingUtilities.invokeLater(() -> {
                SwingUtilities.updateComponentTreeUI(f);
            });
        }
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(loadingPage());
        });
    }

    public JFrame showFrame() {
        createFrame();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }

    public JFrame createFrame() {
        if (frame == null) {
            String t = frameTitle;
            if (t == null) {
                t = "Wizard";
            }
            final JFrame frame = new JFrame(t);
            this.frame = frame;
            frame.setDefaultCloseOperation(isExitOnCloseFrame() ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
//        frame.setResizable(false);

            frame.setIconImage(frameIconImage);
            frame.getContentPane().add(createMainPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
        }
        return frame;
    }

    public JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.add(createLeft(), BorderLayout.LINE_START);
        main.add(createCenter(), BorderLayout.CENTER);
        main.add(createBottom(), BorderLayout.PAGE_END);
        main.setPreferredSize(new Dimension(800, 500));
        applyPlaf();
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.updateComponentTreeUI(main);
        });
        showPage(1);
        return main;
    }


    public JPanel createBottom() {
        JPanel p = new JPanel(new BorderLayout());
        Box line = Box.createHorizontalBox();
        line.add(new JSeparator(JSeparator.HORIZONTAL));
        p.add(line, BorderLayout.PAGE_START);
        Box hb = Box.createHorizontalBox();
        p.add(hb, BorderLayout.CENTER);
        hb.add(Box.createHorizontalGlue());
        hb.add(Box.createRigidArea(new Dimension(10, 40)));
        previousButton = new JButton("Previous");
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onPreviousButton();
            }
        });
        hb.add(previousButton);
        hb.add(Box.createRigidArea(new Dimension(10, 0)));
        nextButton = new JButton("Next");
        hb.add(nextButton);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onNextButton();
            }
        });
        hb.add(Box.createRigidArea(new Dimension(10, 0)));
        cancelButton = new JButton("Cancel");
        hb.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancelButton();
            }
        });
        hb.add(Box.createRigidArea(new Dimension(10, 0)));
        exitButton = new JButton("Finish");
        hb.add(exitButton);
        hb.add(Box.createRigidArea(new Dimension(10, 0)));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onExitButton();
            }
        });
        return p;
    }


    @Override
    public boolean hasNext(int pageIndex) {
        return pageIndex < getPagesCount();
    }

    @Override
    public boolean hasPrevious(int pageIndex) {
        return pageIndex > 1;
    }

    @Override
    public Map<String, Object> getVars() {
        return vars;
    }

    protected abstract WizardPageBase loadingPage();

    protected abstract void createCenterImpl();

    public void startLoading() {
        cardLayout.show(centerPanel, "loading");
        loadingPage().onShow();
    }

    public void stopLoading(int index) {
        loadingPage().onHide();
        cardLayout.show(centerPanel, String.valueOf(index));
    }

    private JPanel createCenter() {
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.add(loadingPage(), "loading");
        loadingPage().onAdd(this, 0);
        createCenterImpl();

        JPanel pp = new JPanel(new BorderLayout());
        pp.add(centerPanel, BorderLayout.CENTER);

        progressBar = new JProgressBar();
        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        pp.add(progressBar, BorderLayout.PAGE_END);
        return pp;
    }


    protected void addPanel(WizardPageBase p) {
        int index = getPagesCount() + 1;
        centerPanel.add(p, String.valueOf(index));
        panels.add(p);
        p.onAdd(this, index);
    }

    private void showPage(int panelIndex) {
        WizardPageBase p = panels.get(panelIndex - 1);
        currentIndex = panelIndex;
        cardLayout.show(centerPanel, String.valueOf(panelIndex));
        getPreviousButton().setEnabled(panelIndex > 1);
        getNextButton().setEnabled(hasNext(panelIndex));
        setProgressByPageIndex();
        p.onShow();
    }

    protected JComponent createLeftImpl() {
        return null;
    }

    public void setLefComponent(JComponent c) {
        leftComponent.removeAll();
        if (c != null) {
            leftComponent.add(c, GBC.of(0, 0).weight(10).insets(5, 10, 5, 10).fillBoth());
        }
    }

    private JComponent createLeft() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(true);
        p.setBorder(BorderFactory.createEtchedBorder());
        setLefComponent(createLeftImpl());
        this.leftComponent = p;
        return p;
    }

}
