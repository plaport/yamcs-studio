/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.csstudio.opibuilder.editor.OPIEditor;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.DisplayEditpart;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.persistence.XMLUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.actions.ActionFactory;

/**
 * The action to copy selected widgets to clipboard.
 */
public class CopyWidgetsAction extends SelectionAction {

    public CopyWidgetsAction(OPIEditor part) {
        super(part);
        setText("Copy");
        setActionDefinitionId("org.eclipse.ui.edit.copy");
        setId(ActionFactory.COPY.getId());
        var sharedImages = part.getSite().getWorkbenchWindow().getWorkbench().getSharedImages();
        setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects().size() == 0
                || getSelectedObjects().size() == 1 && getSelectedObjects().get(0) instanceof EditPart
                        && ((EditPart) getSelectedObjects().get(0)).getModel() instanceof DisplayModel) {
            return false;
        }
        for (var o : getSelectedObjects()) {
            if (o instanceof AbstractBaseEditPart) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {

        var tempModel = new DisplayModel();
        var widgetModels = getSelectedWidgetModels();
        for (var widget : widgetModels) {
            tempModel.addChild(widget, false);
        }

        var xml = XMLUtil.widgetToXMLString(tempModel, false);

        ((OPIEditor) getWorkbenchPart()).getClipboard().setContents(new Object[] { xml },
                new Transfer[] { OPIWidgetsTransfer.getInstance() });
        Display.getCurrent().asyncExec(() -> {
            var pasteAction = ((ActionRegistry) ((OPIEditor) getWorkbenchPart()).getAdapter(ActionRegistry.class))
                    .getAction(ActionFactory.PASTE.getId());
            if (pasteAction != null) {
                ((PasteWidgetsAction) pasteAction).refreshEnable();
            }

        });
    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return a list with all widget models that are currently selected. The order of the selected widgets was kept.
     */
    protected final List<AbstractWidgetModel> getSelectedWidgetModels() {
        List<?> selection = getSelectedObjects();

        List<AbstractWidgetModel> sameParentModels = new ArrayList<>();
        List<AbstractWidgetModel> differentParentModels = new ArrayList<>();
        List<AbstractWidgetModel> result = new ArrayList<>();
        AbstractContainerModel parent = null;
        for (var o : selection) {
            if (o instanceof AbstractBaseEditPart && !(o instanceof DisplayEditpart)) {
                var widgetModel = (AbstractWidgetModel) ((EditPart) o).getModel();
                if (parent == null) {
                    parent = widgetModel.getParent();
                }
                if (widgetModel.getParent() == parent) {
                    sameParentModels.add(widgetModel);
                } else {
                    differentParentModels.add(widgetModel);
                }
            }
        }
        // sort widgets to its original order
        if (sameParentModels.size() > 1) {
            var modelArray = sameParentModels.toArray(new AbstractWidgetModel[0]);

            Arrays.sort(modelArray, (o1, o2) -> {
                if (o1.getParent().getChildren().indexOf(o1) > o2.getParent().getChildren().indexOf(o2)) {
                    return 1;
                } else {
                    return -1;
                }
            });
            result.addAll(Arrays.asList(modelArray));
            if (differentParentModels.size() > 0) {
                result.addAll(differentParentModels);
            }
            return result;
        }
        if (differentParentModels.size() > 0) {
            sameParentModels.addAll(differentParentModels);
        }

        return sameParentModels;
    }
}
