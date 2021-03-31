/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.url;

import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.util.URLViewer;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.gui.editor.NNoteEditorTypeComponent;

/**
 *
 * @author vpc
 */
public class URLNNoteEditorTypeComponent extends JPanel implements NNoteEditorTypeComponent {

    private URLComponent2 comp;
    private URLViewer urlViewer;
    private JLabel error;
    private VNNote currentNode;

    public URLNNoteEditorTypeComponent() {
        super(new BorderLayout());
        comp = new URLComponent2();
        urlViewer = new URLViewer();
        add(comp, BorderLayout.NORTH);
        add(urlViewer, BorderLayout.CENTER);
        add(error = new JLabel(), BorderLayout.SOUTH);
        comp.addListener(new URLComponent2.UrlChangedListener() {
            @Override
            public void onUrlChange(String newURL) {
                  currentNode.setContent(comp.getContentString());
            }
        });
    }

    @Override
    public JComponent component() {
        return this;
    }

    @Override
    public void uninstall() {
    }

    @Override
    public void setNode(VNNote node, NNoteGuiApp sapp) {
        this.currentNode=node;
        String c = node.getContent();
        if (c == null || c.isEmpty()) {
            urlViewer.resetContent();
        } else {
            try {
                urlViewer.load(new URL(c));
                error.setText("");
            } catch (MalformedURLException ex) {
                error.setText(ex.toString());
            }
        }
    }

}
