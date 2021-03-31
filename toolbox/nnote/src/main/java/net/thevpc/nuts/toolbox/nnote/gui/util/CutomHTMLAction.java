/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author vpc
 */
public class CutomHTMLAction extends HTMLEditorKit.HTMLTextAction {

    public CutomHTMLAction(String name, String html,
            HTML.Tag parentTag, HTML.Tag addTag) {
        this(name, html, parentTag, addTag, null, null);
    }

    public CutomHTMLAction(String name, String html,
            HTML.Tag parentTag,
            HTML.Tag addTag,
            HTML.Tag alternateParentTag,
            HTML.Tag alternateAddTag) {
        this(name, html, parentTag, addTag, alternateParentTag,
                alternateAddTag, true);
    }

    /* public */
    CutomHTMLAction(String name, String html,
            HTML.Tag parentTag,
            HTML.Tag addTag,
            HTML.Tag alternateParentTag,
            HTML.Tag alternateAddTag,
            boolean adjustSelection) {
        super(name);
        this.html = html;
        this.parentTag = parentTag;
        this.addTag = addTag;
        this.alternateParentTag = alternateParentTag;
        this.alternateAddTag = alternateAddTag;
        this.adjustSelection = adjustSelection;
    }

    /**
     * A cover for HTMLEditorKit.insertHTML. If an exception it thrown it is
     * wrapped in a RuntimeException and thrown.
     */
    protected void insertHTML(JEditorPane editor, HTMLDocument doc,
            int offset, String html, int popDepth,
            int pushDepth, HTML.Tag addTag) {
        try {
            getHTMLEditorKit(editor).insertHTML(doc, offset, html,
                    popDepth, pushDepth,
                    addTag);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to insert: " + ioe);
        } catch (BadLocationException ble) {
            throw new RuntimeException("Unable to insert: " + ble);
        }
    }

    protected Element commonParent(JEditorPane editor, HTMLDocument doc, int offset) {
        Element commonParent = null;
        Element e;
        boolean isFirst = (offset == 0);

        if (offset > 0) {
            e = doc.getDefaultRootElement();
            while (e != null && e.getStartOffset() != offset
                    && !e.isLeaf()) {
                e = e.getElement(e.getElementIndex(offset));
            }
            commonParent = (e != null) ? e.getParentElement() : null;
        } else {
            // If inserting at the origin, the common parent is the
            // insertElement.
        }
        return commonParent;
    }

    /**
     * This is invoked when inserting at a boundary. It determines the number of
     * pops, and then the number of pushes that need to be performed, and then
     * invokes insertHTML.
     *
     * @since 1.3
     */
    protected void insertAtBoundary(JEditorPane editor, HTMLDocument doc,
            int offset, Element insertElement,
            String html, HTML.Tag parentTag,
            HTML.Tag addTag) {
        // Find the common parent.
        Element e;
        Element commonParent;
        boolean isFirst = (offset == 0);

        if (offset > 0 || insertElement == null) {
            e = doc.getDefaultRootElement();
            while (e != null && e.getStartOffset() != offset
                    && !e.isLeaf()) {
                e = e.getElement(e.getElementIndex(offset));
            }
            commonParent = (e != null) ? e.getParentElement() : null;
        } else {
            // If inserting at the origin, the common parent is the
            // insertElement.
            commonParent = insertElement;
        }
        if (commonParent != null) {
            // Determine how many pops to do.
            int pops = 0;
            int pushes = 0;
            if (isFirst && insertElement != null) {
                e = commonParent;
                while (e != null && !e.isLeaf()) {
                    e = e.getElement(e.getElementIndex(offset));
                    pops++;
                }
            } else {
                e = commonParent;
                offset--;
                while (e != null && !e.isLeaf()) {
                    e = e.getElement(e.getElementIndex(offset));
                    pops++;
                }

                // And how many pushes
                e = commonParent;
                offset++;
                while (e != null && e != insertElement) {
                    e = e.getElement(e.getElementIndex(offset));
                    pushes++;
                }
            }
            pops = Math.max(0, pops - 1);

            // And insert!
            insertHTML(editor, doc, offset, html, pops, pushes, addTag);
        }
    }

    /**
     * If there is an Element with name <code>tag</code> at <code>offset</code>,
     * this will invoke either insertAtBoundary or <code>insertHTML</code>. This
     * returns true if there is a match, and one of the inserts is invoked.
     */
    /*protected*/
    boolean insertIntoTag(JEditorPane editor, HTMLDocument doc,
            int offset, HTML.Tag tag, HTML.Tag addTag) {
        Element e = findElementMatchingTag(doc, offset, tag);
        if (e != null && e.getStartOffset() == offset) {
            insertAtBoundary(editor, doc, offset, e, html,
                    tag, addTag);
            return true;
        } else if (offset > 0) {
            int depth = elementCountToTag(doc, offset - 1, tag);
            if (depth != -1) {
                insertHTML(editor, doc, offset, html, depth, 0, addTag);
                return true;
            }
        }
        return false;
    }

    /**
     * Called after an insertion to adjust the selection.
     */
    /* protected */
    void adjustSelection(JEditorPane pane, HTMLDocument doc,
            int startOffset, int oldLength) {
        int newLength = doc.getLength();
        if (newLength != oldLength && startOffset < newLength) {
            if (startOffset > 0) {
                String text;
                try {
                    text = doc.getText(startOffset - 1, 1);
                } catch (BadLocationException ble) {
                    text = null;
                }
                if (text != null && text.length() > 0
                        && text.charAt(0) == '\n') {
                    pane.select(startOffset, startOffset);
                } else {
                    pane.select(startOffset + 1, startOffset + 1);
                }
            } else {
                pane.select(1, 1);
            }
        }
    }

    /**
     * Inserts the HTML into the document.
     *
     * @param ae the event
     */
    public void actionPerformed(ActionEvent ae) {
        JEditorPane editor = getEditor(ae);
        if (editor != null) {
            HTMLDocument doc = getHTMLDocument(editor);
            int offset = editor.getSelectionStart();
            int length = doc.getLength();
            boolean inserted=false;
            // Try first choice
            if (!insertIntoTag(editor, doc, offset, parentTag, addTag)) {
                // Then alternate.
                if (alternateParentTag != null) {
                    inserted = insertIntoTag(editor, doc, offset,
                            alternateParentTag,
                            alternateAddTag);
                }
                if (!inserted) {
                    Element commonParent = commonParent(editor, doc, offset);
                    Element e = null;
                    if (commonParent != null) {
                        Element insertElement = null;
                        boolean isFirst = true;
                        int pops = 0;
                        int pushes = 0;
                        if (isFirst && insertElement != null) {
                            e = commonParent;
                            while (e != null && !e.isLeaf()) {
                                e = e.getElement(e.getElementIndex(offset));
                                pops++;
                            }
                        } else {
                            e = commonParent;
                            offset--;
                            while (e != null && !e.isLeaf()) {
                                e = e.getElement(e.getElementIndex(offset));
                                pops++;
                            }

                            // And how many pushes
                            e = commonParent;
                            offset++;
                            while (e != null && e != insertElement) {
                                e = e.getElement(e.getElementIndex(offset));
                                pushes++;
                            }
                        }
                        pops = Math.max(0, pops - 1);
                        pushes = Math.max(0, pushes - 2);

                        // And insert!
                        insertHTML(editor, doc, offset, html, pops, pushes, addTag);
                    }
                }
            } else {
                inserted = true;
            }
            if (adjustSelection && inserted) {
                adjustSelection(editor, doc, offset, length);
            }
        }
    }

    /**
     * HTML to insert.
     */
    protected String html;
    /**
     * Tag to check for in the document.
     */
    protected HTML.Tag parentTag;
    /**
     * Tag in HTML to start adding tags from.
     */
    protected HTML.Tag addTag;
    /**
     * Alternate Tag to check for in the document if parentTag is not found.
     */
    protected HTML.Tag alternateParentTag;
    /**
     * Alternate tag in HTML to start adding tags from if parentTag is not found
     * and alternateParentTag is found.
     */
    protected HTML.Tag alternateAddTag;
    /**
     * True indicates the selection should be adjusted after an insert.
     */
    boolean adjustSelection;

}
