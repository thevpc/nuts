/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.thevpc.nuts.installer.panels;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.LayoutConstants;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.data.GUIInstallData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * The MyHelloPanel panel class.
 *
 * @author Julien Ponge
 */
public class MyHelloPanel extends IzPanel
{
    private static final long serialVersionUID = 3257848774955905587L;

    /**
     * The constructor.
     *
     * @param parent The parent.
     * @param idata  The installation installDataGUI.
     */
    public MyHelloPanel(Panel panel, InstallerFrame parent, GUIInstallData idata, Resources resources, Log log)
    {
        this(panel, parent, idata, new IzPanelLayout(log), resources);
    }

    /**
     * Creates a new MyHelloPanel object with the given layout manager. Valid layout manager are the
     * IzPanelLayout and the GridBagLayout. New panels should use the IzPanelLayout. If lm is
     * null, no layout manager will be created or initialized.
     *
     * @param parent The parent IzPack installer frame.
     * @param idata  The installer internal installDataGUI.
     * @param layout layout manager to be used with this IzPanel
     */
    public MyHelloPanel(Panel panel, InstallerFrame parent, GUIInstallData idata, LayoutManager2 layout, Resources resources)
    {
        // Layout handling. This panel was changed from a mixed layout handling
        // with GridBagLayout and BoxLayout to IzPanelLayout. It can be used as an
        // example how to use the IzPanelLayout. For this there are some comments
        // which are excrescent for a "normal" panel.
        // Set a IzPanelLayout as layout for this panel.
        // This has to be the first line during layout if IzPanelLayout will be used.

        //com.izforge.izpack.installer.gui.InstallerFrame cannot be converted to com.izforge.izpack.api.data.Panel
	/*
	public IzPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources)
	*/
        super(panel, parent, idata, layout, resources);
        // We create and put the labels
        Messages messages = installData.getMessages();
        String welcomeText = messages.get("MyHelloPanel.welcome1") + idata.getInfo().getAppName() + " version "
                + idata.getInfo().getAppVersion() + messages.get("MyHelloPanel.welcome2");
        JLabel welcomeLabel = LabelFactory.create(welcomeText, parent.getIcons().get("host"), LEADING);
        welcomeLabel.setName(GuiId.HELLO_PANEL_LABEL.id);
        // IzPanelLayout is a constraint orientated layout manager. But if no constraint is
        // given, a default will be used. It starts in the first line.
        // NEXT_LINE have to insert also in the first line!!
        add(welcomeLabel, NEXT_LINE);
        // Yes, there exist also a strut for the IzPanelLayout.
        // But the strut will be only used for one cell. A vertical strut will be use
        // NEXT_ROW, a horizontal NEXT_COLUMN. For more information see the java doc.
        // add(IzPanelLayout.createVerticalStrut(20));
        // But for a strut you have to define a fixed height. Alternative it is possible
        // to create a paragraph gap which is configurable.
        add(IzPanelLayout.createParagraphGap());

        ArrayList<Info.Author> authors = idata.getInfo().getAuthors();

        if (!authors.isEmpty())
        {
            String authorText = messages.get("MyHelloPanel.authors");
            JLabel appAuthorsLabel = LabelFactory.create(authorText, parent.getIcons()
                    .get("information"), LEADING);
            // If nothing is added to the IzPanelLayout the position of an add will be
            // determined in the default constraint. For labels, it is CURRENT_ROW, NEXT_COLUMN.
            // But at this point we would place the label in the next row. It is possible
            // to create an IzPanelConstraint with these options, but it is also possible to
            // use simple the NEXT_LINE object as constraint. Attention!! Do not use
            // LayoutConstants.NEXT_ROW else LayoutConstants.NEXT_LINE because NEXT_ROW is an
            // int and with it another add method will be used without any warning (there the
            // parameter will be used as position of the component in the panel, not the
            // layout manager).
            add(appAuthorsLabel, LayoutConstants.NEXT_LINE);

            for (Info.Author author : authors)
            {
                String email = (author.getEmail() != null && author.getEmail().length() > 0) ? (" <"
                        + author.getEmail() + ">") : "";
                JLabel label = LabelFactory.create(" - " + author.getName() + email, parent.getIcons()
                        .get("empty"), LEADING);
                add(label, NEXT_LINE);
            }
            add(IzPanelLayout.createParagraphGap());
        }

        if (idata.getInfo().getAppURL() != null)
        {
            String urlText = messages.get("MyHelloPanel.url") + idata.getInfo().getAppURL();
            JLabel appURLLabel = LabelFactory.create(urlText, parent.getIcons().get("bookmark"),
                    LEADING);
            add(appURLLabel, LayoutConstants.NEXT_LINE);
        }
        // At end of layout we should call the completeLayout method also they do nothing.
        getLayoutHelper().completeLayout();
    }

    /**
     * Indicates whether the panel has been validated or not.
     *
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }
}
