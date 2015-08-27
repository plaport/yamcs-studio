package org.yamcs.studio.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.yamcs.protobuf.Rest.RestGetParameterInfoRequest;
import org.yamcs.protobuf.Rest.RestGetParameterInfoResponse;
import org.yamcs.protobuf.Rest.RestParameterInfo;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;

import com.google.protobuf.MessageLite;

/**
 * Show detailed information of a widget's PVs.
 * <p>
 * If it's a yamcs parameter, the information is enriched, otherwise show the typical CS-Studio
 * content.
 */
public class ShowPVInfoAction implements IObjectActionDelegate {

    private static final Logger log = Logger.getLogger(ShowPVInfoAction.class.getName());

    private IStructuredSelection selection;
    private IWorkbenchPart targetPart;

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    @Override
    public void run(IAction action) {
        if (getSelectedWidget() == null ||
                getSelectedWidget().getAllPVs() == null ||
                getSelectedWidget().getAllPVs().isEmpty()) {
            MessageDialog.openInformation(null, "No PV", "There are no related PVs for this widget");
            return;
        }

        List<PVInfo> pvInfos = new ArrayList<>();
        getSelectedWidget().getAllPVs().forEach((k, v) -> pvInfos.add(new PVInfo(k, v)));
        Collections.sort(pvInfos);
        loadParameterInfoAndShowDialog(pvInfos);
    }

    /**
     * Gets detailed information on yamcs parameters. We do this one-by-one, because otherwise we
     * risk having one invalid parameter spoil the whole bunch. Idealy we would rewrite this API a
     * bit on yamcs server, so we avoid the use of a latch.
     */
    private void loadParameterInfoAndShowDialog(List<PVInfo> pvInfos) {
        RestClient restClient = YamcsPlugin.getDefault().getRestClient();
        if (restClient == null) {
            // TODO get rid of this, and add check to enabledWhen state instead
            MessageDialog.openWarning(null, "Not Connected", "You are not currently connected to Yamcs");
            return;
        }

        List<PVInfo> yamcsPvs = new ArrayList<>();
        for (PVInfo pvInfo : pvInfos)
            if (pvInfo.isYamcsParameter())
                yamcsPvs.add(pvInfo);

        // Start a worker thread that will show the dialog when a response for all
        // yamcs parameters arrived
        new Thread() {

            @Override
            public void run() {
                CountDownLatch latch = new CountDownLatch(yamcsPvs.size());

                // Another reason why we should have futures
                for (PVInfo pvInfo : pvInfos) {
                    if (!pvInfo.isYamcsParameter()) {
                        latch.countDown();
                        continue;
                    }

                    RestGetParameterInfoRequest.Builder requestb = RestGetParameterInfoRequest.newBuilder();
                    requestb.addList(NamedObjectId.newBuilder().setName(pvInfo.getDisplayName()));
                    restClient.getParameterInfo(requestb.build(), new ResponseHandler() {
                        @Override
                        public void onMessage(MessageLite responseMsg) {
                            RestGetParameterInfoResponse response = (RestGetParameterInfoResponse) responseMsg;
                            List<RestParameterInfo> infos = response.getPinfoList();
                            if (infos.isEmpty())
                                pvInfo.setParameterInfoException("Not authorised");
                            else
                                pvInfo.setParameterInfo(infos.get(0));
                            latch.countDown();
                        }

                        @Override
                        public void onException(Exception e) {
                            log.log(Level.WARNING, "Could not fetch yamcs parameter info", e);
                            pvInfo.setParameterInfoException(e.getMessage());
                            latch.countDown();
                        }
                    });
                }

                try {
                    latch.await();
                    targetPart.getSite().getShell().getDisplay().asyncExec(() -> showDialog(pvInfos));
                } catch (InterruptedException e) {
                    targetPart.getSite().getShell().getDisplay().asyncExec(() -> {
                        log.log(Level.SEVERE, "Could not fetch yamcs parameter info", e);
                        MessageDialog.openError(null, "Could Not Fetch Yamcs Parameter Info", "Interrupted while fetching yamcs parameter info");
                    });
                }
            }
        }.start();
    }

    private void showDialog(List<PVInfo> pvInfos) {
        PVInfoDialog dialog = new PVInfoDialog(
                targetPart.getSite().getShell(), "PV Info", pvInfos);
        dialog.open();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection)
            this.selection = (IStructuredSelection) selection;
    }

    private AbstractBaseEditPart getSelectedWidget() {
        if (selection.getFirstElement() instanceof AbstractBaseEditPart)
            return (AbstractBaseEditPart) selection.getFirstElement();
        else
            return null;
    }
}
