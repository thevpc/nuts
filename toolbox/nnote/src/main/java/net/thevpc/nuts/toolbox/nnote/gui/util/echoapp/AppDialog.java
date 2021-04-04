/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util.echoapp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import net.thevpc.common.swing.GridBagLayoutSupport;
import net.thevpc.common.swing.JDialog2;
import net.thevpc.common.swing.SwingUtilities3;
import net.thevpc.echo.Application;

/**
 *
 * @author vpc
 */
public class AppDialog extends JDialog2 {

    private GenFooter footer;
    private JComponent mainComponent;
    protected Application app;
    protected String selectedButton;
    protected String[] buttonIds;

    public static Builder of(Application app) {
        return new Builder(app);
    }

    public static class AppDialogContext {

        private JDialog dialog;
        private String buttonId;
        private int buttonIndex;

        public AppDialogContext(JDialog dialog, String buttonId, int buttonIndex) {
            this.dialog = dialog;
            this.buttonId = buttonId;
            this.buttonIndex = buttonIndex;
        }

        public void closeDialog() {
            getDialog().setVisible(false);
        }

        public JDialog getDialog() {
            return dialog;
        }

        public String getButtonId() {
            return buttonId;
        }

        public int getButtonIndex() {
            return buttonIndex;
        }

    }

    public static interface DialogAction {

        void onAction(AppDialogContext context);
    }

    public static class Builder {

        private Application app;
        private String titleId;
        private JComponent mainComponent;
        private List<String> buttonIds;
        private String defaultId;
        private Map<String, DialogAction> consMap = new HashMap<>();
        private Dimension preferredSize;
        private DialogAction onClose = c -> c.closeDialog();

        public Builder(Application app) {
            this.app = app;
        }

        public Dimension getPreferredSize() {
            return preferredSize;
        }

        public Builder setPreferredSize(Dimension preferredSize) {
            this.preferredSize = preferredSize;
            return this;
        }

        public Builder setPreferredSize(int width, int heigth) {
            this.preferredSize = new Dimension(width, heigth);
            return this;
        }

        public Builder setTitleId(String titleId) {
            this.titleId = titleId;
            return this;
        }

        public Builder setInputTextFieldContent(String headerId, String initialValue) {
            return setInputContent(new InputTextFieldPanel(app, headerId, initialValue));
        }

        public Builder setInputTextAreadContent(String headerId, String initialValue) {
            return setInputContent(new InputTextAreaPanel(app, headerId, initialValue));
        }

        public Builder setInputContent(InputPanel inputPanel) {
            return setContent((JComponent) inputPanel);
        }

        public Builder setContentText(String labelId) {
            return setContent(new JLabel(labelId));
        }

        public Builder setContentTextId(String labelId) {
            return setContent(new JLabel(app.i18n().getString(labelId)));
        }

        public Builder setContent(JComponent mainComponent) {
            this.mainComponent = mainComponent;
            return this;
        }

        public Builder withOkOnlyButton() {
            return withOkOnlyButton(onClose);
        }

        public Builder withOkOnlyButton(DialogAction ok) {
            buttonIds = Arrays.asList("ok");
            consMap.put("ok", ok);
            return this;
        }

        public Builder withOkCancelButtons() {
            return withOkCancelButtons(onClose, onClose);
        }

        public Builder withOkCancelButtons(DialogAction ok, DialogAction cancel) {
            buttonIds = Arrays.asList("ok", "cancel");
            consMap.put("ok", ok);
            consMap.put("cancel", cancel);
            return this;
        }

        public Builder withYesNoButtons() {
            return withYesNoButtons(onClose, onClose);
        }

        public Builder withYesNoButtons(DialogAction yes, DialogAction no) {
            buttonIds = Arrays.asList("yes", "no");
            consMap.put("yes", yes);
            consMap.put("no", no);
            return this;
        }

        public Builder withYesNoCancelButtons(DialogAction yes, DialogAction no, DialogAction cancel) {
            buttonIds = Arrays.asList("yes", "no", "cancel");
            consMap.put("yes", yes);
            consMap.put("no", no);
            consMap.put("cancel", cancel);
            return this;
        }

        public Builder withButtons(String... buttonIds) {
            this.buttonIds = buttonIds == null ? null : Arrays.asList(buttonIds);
            return this;
        }

        public Builder setDefaultId(String defaultId) {
            this.defaultId = defaultId;
            return this;
        }

        public Builder setButtonHandler(String s, DialogAction r) {
            consMap.put(s, r);
            return this;
        }

        public String showDialog() {
            return build().showDialog();
        }

        public DialogResult showInputDialog() {
            return build().showInputDialog();
        }

        public AppDialog build() {
            String _titleId = titleId;
            JComponent _mainComponent = mainComponent;
            List<String> _buttonIds = buttonIds == null ? null : new ArrayList<>(buttonIds);
            String _defaultId = defaultId;
            DialogAction _cons = null;
            if (_titleId == null) {
                _titleId = "Message.defaultTitle";
            }
            if (_mainComponent == null) {
                _mainComponent = new JLabel();
            }
            if (_buttonIds.isEmpty()) {
                _buttonIds.add("ok");
            }
            if (_cons == null) {
                _cons = new DialogAction() {
                    @Override
                    public void onAction(AppDialogContext context) {
                        DialogAction a = consMap.get(context.buttonId);
                        if (a != null) {
                            a.onAction(context);
                        }
                    }
                };
            }
            AppDialog a = new AppDialog(app, _titleId);
            if (preferredSize != null) {
                a.setPreferredSize(preferredSize);
            }
            a.build(_mainComponent, _buttonIds.toArray(new String[0]), _defaultId, _cons);
            return a;
        }
    }

    public AppDialog(Application app, String titleId, JComponent mainComponent, String[] buttonIds, String defaultId, DialogAction cons) {
        this(app, titleId);
        build(mainComponent, buttonIds, defaultId, cons);
    }

    public AppDialog(Application app, String titleId) {
        super((JFrame) app.mainWindow().get().component(),
                app.i18n().getString(titleId), true
        );
        this.app = app;
    }

    public DialogResult showInputDialog() {
        String a = showDialog();
        return new DialogResult() {
            @Override
            public String getButtonId() {
                return a;
            }

            @Override
            public <T> T getValue() {
                if (mainComponent == null || !(mainComponent instanceof InputPanel)) {
                    throw new IllegalArgumentException("Not an input component");
                }
                return (T) ((InputPanel) mainComponent).getValue();
            }
        };
    }

    public String showDialog() {
        this.selectedButton = null;
        setVisible(true);
        return selectedButton;
    }

    protected void build(JComponent mainComponent, String[] buttonIds, String defaultId, DialogAction cons) {
        getRootPane().setLayout(new BorderLayout());
        footer = new GenFooter(app, cons, buttonIds);
        JPanel withBorder=new JPanel(new BorderLayout());
        withBorder.add(mainComponent);
        withBorder.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        getRootPane().add(withBorder, BorderLayout.CENTER);
        getRootPane().add(footer, BorderLayout.SOUTH);
        SwingUtilities3.addEscapeBindings(this);
        if (defaultId != null) {
            this.getRootPane().setDefaultButton(footer.getButton(defaultId));
        }
        this.setLocationRelativeTo(getOwner());
        pack();
    }

    public static class ActionInfo {

        int index;
        String id;

        public ActionInfo(int index, String id) {
            this.index = index;
            this.id = id;
        }

    }

    public class GenFooter extends JPanel {

        Map<String, JButton> buttons = new LinkedHashMap<>();
        private ActionListenerImpl actionListenerImpl;

        public GenFooter(Application app, DialogAction cons, String[] buttonIds) {
            StringBuilder form = new StringBuilder("[-=glue(h)]");
            actionListenerImpl = new ActionListenerImpl(cons);
            for (int i = 0; i < buttonIds.length; i++) {
                String id = buttonIds[i];
                JButton b = new JButton(app.i18n().getString("Message." + id));
                b.putClientProperty("ActionInfo", new ActionInfo(i, id));
                buttons.put(id, b);
                form.append("[b" + i + "]");
                b.addActionListener(actionListenerImpl);
            }
            form.append(" ; insets(5)");
            GridBagLayoutSupport a = new GridBagLayoutSupport(form.toString());
            int index = 0;
            for (Map.Entry<String, JButton> entry : buttons.entrySet()) {
                a.bind("b" + index, entry.getValue());
                index++;
            }
            a.apply(this);
            setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        }

        public JButton getButton(String id) {
            return buttons.get(id);
        }

    }

    private class ActionListenerImpl implements ActionListener {

        private final DialogAction cons;

        public ActionListenerImpl(DialogAction cons) {
            this.cons = cons;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ActionInfo ci = (ActionInfo) ((JButton) e.getSource()).getClientProperty("ActionInfo");
            selectedButton = ci.id;
            cons.onAction(new AppDialogContext(AppDialog.this, ci.id, ci.index));
        }
    }

    public static interface DialogResult {

        public String getButtonId();

        public <T> T getValue();

        public default boolean isBlankValue() {
            Object o = getValue();
            if (o == null) {
                return true;
            }
            if (o instanceof String) {
                return ((String) o).trim().isEmpty();
            }
            return false;
        }

        public default boolean isButton(String ok) {
            return Objects.equals(ok, getButtonId());
        }
    }

    public static interface InputPanel {

        Object getValue();
    }

    public static class InputTextFieldPanel extends JPanel implements InputPanel {

        private JLabel header = new JLabel();
        private JTextField value = new JTextField();

        public InputTextFieldPanel(Application app, String headerId, String initalValue) {
            header.setText(app.i18n().getString(headerId));
            value.setText(initalValue);
            GridBagLayoutSupport.of("[^<header]\n[-=<value];insets(5,5,5,5)")
                    .bind("header", header)
                    .bind("value", value)
                    .apply(this);
        }

        @Override
        public Object getValue() {
            return value.getText();
        }

    }

    public static class InputTextAreaPanel extends JPanel implements InputPanel {

        private JLabel header = new JLabel();
        private JTextArea value = new JTextArea();

        public InputTextAreaPanel(Application app, String headerId, String initalValue) {
            header.setText(app.i18n().getString(headerId));
            value.setText(initalValue);
            GridBagLayoutSupport.of("[^<header]\n[-==<$+value];insets(5,5,5,5)")
                    .bind("header", header)
                    .bind("value", new JScrollPane(value))
                    .apply(this);
        }

        @Override
        public Object getValue() {
            return value.getText();
        }

    }

}
