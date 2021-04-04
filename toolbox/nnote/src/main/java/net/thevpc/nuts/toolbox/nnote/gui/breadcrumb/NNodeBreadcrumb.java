/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.breadcrumb;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.thevpc.common.swing.JBreadCrumb;
import net.thevpc.common.swing.ObjectListModel;
import net.thevpc.common.swing.ObjectListModelListener;
import net.thevpc.common.swing.SwingUtilities3;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;
import net.thevpc.nuts.toolbox.nnote.gui.tree.VNNoteSelectionListener;
import net.thevpc.nuts.toolbox.nnote.gui.util.DefaultObjectListModel;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;

/**
 *
 * @author vpc
 */
public class NNodeBreadcrumb extends JScrollPane {

    public NNodeBreadcrumb(NNoteGuiApp sapp) {
        JBreadCrumb b = new JBreadCrumb();
        b.setOpaque(false);
        b.setBorder(null);
        //sapp.
        sapp.tree().addNoteSelectionListener(new VNNoteSelectionListener() {
            @Override
            public void onSelectionChanged(VNNote note) {
                SwingUtilities3.invokeLater(() -> {
                    b.setModel(createBreadCrumModel(note));
                    NNodeBreadcrumb.this.invalidate();
                    NNodeBreadcrumb.this.revalidate();
                    getHorizontalScrollBar().invalidate();
                    getHorizontalScrollBar().revalidate();
//                    NNodeBreadcrumb.this.getViewport().fireStateChanged();
                    getHorizontalScrollBar().setValue(getHorizontalScrollBar().getMaximum());
                });
            }

            private ObjectListModel createBreadCrumModel(VNNote note) {
                List<VNNote> bm = new ArrayList<>();
                VNNote n = note;
                while (n != null) {
                    bm.add(0, n);
                    n = n.getParent();
                }
                if (bm.size() > 0) {
                    bm.remove(0);//remove root!
                }
                DefaultObjectListModel model = new DefaultObjectListModel(bm);
                return model;
            }
        });
        this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setMinimumSize(new Dimension(60, 36));
        this.setPreferredSize(new Dimension(60, 36));
        this.setViewportView(b);
        b.addListener(new ObjectListModelListener() {
            @Override
            public void onSelected(Object component, int index) {
                VNNote v = (VNNote) component;
                sapp.tree().setSelectedNote(v);
            }
        });
    }

}
