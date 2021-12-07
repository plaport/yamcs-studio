package org.yamcs.studio.commanding.stack;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class DeleteHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShellChecked(event);

        var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPart part = window.getActivePage().findView(CommandStackView.ID);
        var commandStackView = (CommandStackView) part;

        var sel = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        if (MessageDialog.openConfirm(shell, "Confirm Deletion",
                "Are you sure you want to delete the selected commands from the stack?")) {
            CommandStack.getInstance().getCommands().removeAll(sel.toList());
            commandStackView.refreshState();
        }

        return null;
    }
}
