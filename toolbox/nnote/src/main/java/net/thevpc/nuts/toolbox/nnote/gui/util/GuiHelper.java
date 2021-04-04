/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import net.thevpc.common.swing.UndoRedoHelper;
import net.thevpc.jeep.editor.JSyntaxDocument;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;

/**
 *
 * @author vpc
 */
public class GuiHelper {

    public static Font deriveFont(Font _font, boolean bold, boolean italic, boolean underline, boolean strike) {
        Font f = _font.deriveFont((bold ? Font.BOLD : 0) + (italic ? Font.ITALIC : 0));
        Map attributes = null;
        if (underline) {
            if (attributes == null) {
                attributes = f.getAttributes();
            }
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }
        if (strike) {
            if (attributes == null) {
                attributes = f.getAttributes();
            }
            attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        }
        if (attributes != null) {
            f = f.deriveFont(attributes);
        }
        return f;
    }

    public static Color parseColor(String s) {
        if (OtherUtils.isBlank(s)) {
            return null;
        }
        if (s.indexOf(",") > 0) {
            String r = null;
            String g = null;
            String b = null;
            String[] sp = s.split(",");
            if (sp.length == 3) {
                try {
                    return new Color(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
                } catch (Exception ex) {
                }
            }
            return null;
        } else if (s.matches("#[a-fA-F0-9]{12}")) {
            try {
                return new Color(Integer.parseInt(s.substring(1, 5), 16), Integer.parseInt(s.substring(5, 9), 16), Integer.parseInt(s.substring(9, 13), 16));
            } catch (Exception ex) {
                //
            }
        } else if (s.matches("[a-fA-F0-9]{12}")) {
            try {
                return new Color(Integer.parseInt(s.substring(0, 4), 16), Integer.parseInt(s.substring(4, 8), 16), Integer.parseInt(s.substring(8, 12), 16));
            } catch (Exception ex) {
                //
            }
        }
        return null;
    }

    public static String formatColor(Color s) {
        if (s == null) {
            return "";
        }
        return "#" + OtherUtils.toHex(s.getRed(), 4) + OtherUtils.toHex(s.getGreen(), 4) + OtherUtils.toHex(s.getBlue(), 4);
    }

    public static void installUndoRedoManager(JTextComponent c) {
        Document d = c.getDocument();
        if (d instanceof JSyntaxDocument) {
            UndoManager v = ((JSyntaxDocument) d).getUndoManager();
            UndoRedoHelper.installUndoRedoManager(c, v);
        } else {
            UndoRedoHelper.installUndoRedoManager(c, new UndoManager());
        }
    }
    
}
