/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.wizards;

import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

/**
 * A wizard for creating new OPI Files.
 */
public class NewOPIFileWizard extends Wizard implements INewWizard {

    private NewOPIFileWizardPage opiFilePage;

    private IStructuredSelection selection;

    private IWorkbench workbench;

    @Override
    public void addPages() {
        opiFilePage = new NewOPIFileWizardPage("OPIFilePage", selection);
        addPage(opiFilePage);
    }

    @Override
    public boolean performFinish() {
        var file = opiFilePage.createNewFile();

        if (file == null) {
            return false;
        }

        try {
            workbench.getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(file),
                    "org.csstudio.opibuilder.OPIEditor");
        } catch (PartInitException e) {
            MessageDialog.openError(null, "Open OPI File error",
                    "Failed to open the newly created OPI File. \n" + e.getMessage());
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "OPIEditor activation error", e);
        }

        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }
}
