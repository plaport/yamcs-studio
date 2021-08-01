package org.yamcs.studio.eventlog;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.CreateEventRequest;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.protobuf.Timestamp;

public class AddManualEventDialog extends TitleAreaDialog {

    private Calendar generationTimeValue = null;

    private Text messageText;
    private CDateTime generationDatePicker;
    private Combo severityCombo;

    protected AddManualEventDialog(Shell shell) {
        super(shell);
    }

    protected AddManualEventDialog(Shell shell, Instant generationTime) {
        super(shell);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(generationTime, YamcsPlugin.getZoneId());
        generationTimeValue = GregorianCalendar.from(zdt);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Add a Manual Event");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 20;
        layout.marginWidth = 20;

        layout.verticalSpacing = 5;
        container.setLayout(layout);

        Label lbl = new Label(container, SWT.NONE);
        lbl.setText("Message");
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        lbl.setLayoutData(gd);
        gd.verticalAlignment = SWT.TOP;
        messageText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalAlignment = SWT.TOP;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        messageText.setLayoutData(gd);
        GC gc = new GC(messageText);
        try {
            gc.setFont(messageText.getFont());
            FontMetrics fm = gc.getFontMetrics();
            gd.heightHint = 5 * fm.getHeight();
        } finally {
            gc.dispose();
        }
        messageText.setText("");

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Event Time");
        generationDatePicker = new CDateTime(container,
                CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);
        if (generationTimeValue != null) {
            generationDatePicker.setSelection(generationTimeValue.getTime());
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Severity");
        severityCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        severityCombo.add(EventSeverity.INFO.name());
        severityCombo.add(EventSeverity.WATCH.name());
        severityCombo.add(EventSeverity.WARNING.name());
        severityCombo.add(EventSeverity.DISTRESS.name());
        severityCombo.add(EventSeverity.CRITICAL.name());
        severityCombo.add(EventSeverity.SEVERE.name());
        severityCombo.select(0);

        return container;
    }

    @Override
    protected void okPressed() {
        String message = messageText.getText();
        Instant time = null;
        if (generationDatePicker.hasSelection()) {
            time = generationDatePicker.getSelection().toInstant();
        }
        String severityString = severityCombo.getItem(severityCombo.getSelectionIndex());
        EventSeverity severity = EventSeverity.valueOf(severityString);

        YamcsClient client = YamcsPlugin.getYamcsClient();

        CreateEventRequest.Builder requestb = CreateEventRequest.newBuilder();
        requestb.setInstance(YamcsPlugin.getInstance());
        requestb.setMessage(message);
        requestb.setSeverity(severity.toString());

        if (time != null) {
            OffsetDateTime t = time.atOffset(ZoneOffset.UTC);
            requestb.setTime(Timestamp.newBuilder().setSeconds(t.toEpochSecond()).setNanos(t.getNano()));
        }

        client.createEvent(requestb.build()).whenComplete((data, exc) -> {
            if (exc == null) {
                Display.getDefault().asyncExec(() -> close());
            } else {
                Display.getDefault().asyncExec(() -> {
                    MessageBox m = new MessageBox(getShell(),
                            SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL);
                    m.setText("Error");
                    m.setMessage(exc.getMessage());
                    m.open();
                });
            }
        });

    }

}
