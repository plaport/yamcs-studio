/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.script.RulesInput;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The cell editor for rules input.
 */
public class RulesInputCellEditor extends AbstractDialogCellEditor {

    private RulesInput rulesInput;

    private AbstractWidgetModel widgetModel;

    public RulesInputCellEditor(Composite parent, AbstractWidgetModel widgetModel, String title) {
        super(parent, title);
        this.widgetModel = widgetModel;
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {
        var dialog = new RulesInputDialog(parentShell, rulesInput, widgetModel, dialogTitle);

        if (dialog.open() == Window.OK) {
            rulesInput = new RulesInput(dialog.getRuleDataList());
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return rulesInput != null;
    }

    @Override
    protected Object doGetValue() {
        return rulesInput;
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof RulesInput)) {
            rulesInput = new RulesInput();
        } else {
            rulesInput = (RulesInput) value;
        }
    }
}
