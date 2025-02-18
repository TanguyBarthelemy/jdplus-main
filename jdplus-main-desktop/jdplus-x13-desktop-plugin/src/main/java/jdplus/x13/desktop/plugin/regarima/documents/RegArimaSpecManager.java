/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.x13.desktop.plugin.regarima.documents;

import jdplus.x13.desktop.plugin.regarima.descriptors.RegArimaSpecUI;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.PropertiesDialog;
import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import jdplus.x13.base.workspace.X13Handlers;

import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jean Palate
 * @author Mats Maggi
 */
@ServiceProvider(service = WorkspaceItemManager.class,
        position = 1000)
public class RegArimaSpecManager extends AbstractWorkspaceItemManager<RegArimaSpec> {

    public static final LinearId ID = new LinearId(RegArimaSpec.FAMILY, "specifications", RegArimaSpec.METHOD);
    public static final String PATH = "regarima.spec";
    public static final String ITEMPATH = "regarima.spec.item";

    @Override
    protected String getItemPrefix() {
        return X13Handlers.REGARIMASPEC_PREFIX;
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public RegArimaSpec createNewObject() {
        return RegArimaSpec.RG4;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.Spec;
    }

    @Override
    public String getActionsPath() {
        return PATH;
    }

    @Override
    public Status getStatus() {
        return Status.Certified;
    }

    @Override
    public Action getPreferredItemAction(Id child) {
         final WorkspaceItem<RegArimaSpec> xdoc = WorkspaceFactory.getInstance().getActiveWorkspace().searchDocument(child, RegArimaSpec.class);
        if (xdoc == null || xdoc.getElement() == null) {
            return null;
        }
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit(xdoc);
            }
        };
    }

    @Override
    public Icon getManagerIcon() {
        return ImageUtilities.loadImageIcon("jdplus/x13/desktop/plugin/x13/api/blog_16x16.png", false);
   }

    @Override
    public Icon getItemIcon(WorkspaceItem<RegArimaSpec> doc) {
        return getManagerIcon();
    }

    @Override
    public List<WorkspaceItem<RegArimaSpec>> getDefaultItems() {
        ArrayList<WorkspaceItem<RegArimaSpec>> defspecs = new ArrayList<>();
        defspecs.add(WorkspaceItem.system(ID, "RG0", RegArimaSpec.RG0));
        defspecs.add(WorkspaceItem.system(ID, "RG1", RegArimaSpec.RG1));
        defspecs.add(WorkspaceItem.system(ID, "RG2", RegArimaSpec.RG2));
        defspecs.add(WorkspaceItem.system(ID, "RG3", RegArimaSpec.RG3));
        defspecs.add(WorkspaceItem.system(ID, "RG4", RegArimaSpec.RG4));
        defspecs.add(WorkspaceItem.system(ID, "RG5", RegArimaSpec.RG5));
        return defspecs;
    }

    public void edit(final WorkspaceItem<RegArimaSpec> xdoc) {
        if (xdoc == null || xdoc.getElement() == null) {
            return;

        }
        final RegArimaSpecUI ui = new RegArimaSpecUI(xdoc.getElement(), xdoc.isReadOnly());
        Frame owner = WindowManager.getDefault().getMainWindow();
        PropertiesDialog propDialog =
                new PropertiesDialog(owner, true, ui,
                new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                xdoc.setElement(ui.getCore());
            }
        });
        propDialog.setTitle(xdoc.getDisplayName());
        propDialog.setLocationRelativeTo(owner);
        propDialog.setVisible(true);
    }

    @Override
    public Class getItemClass() {
        return RegArimaSpec.class;
    }
}
