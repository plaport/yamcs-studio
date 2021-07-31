package org.yamcs.studio.archive;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

public class TimeLocator extends Drawable {

    private TimeProvider timeProvider;

    public TimeLocator(Timeline timeline, TimeProvider timeProvider) {
        super(timeline);
        this.timeProvider = timeProvider;
    }

    @Override
    void drawOverlay(GC gc) {
        var t = timeProvider.getTime();
        if (t != null) {
            var x = (int) Math.round(timeline.positionTime(t));

            var foreground = timeline.getDisplay().getSystemColor(SWT.COLOR_RED);
            gc.setForeground(foreground);
            gc.setLineDash(new int[] { 4, 3 });
            gc.drawLine(x, 0, x, timeline.getBounds().height);
            gc.setLineDash(null);
        }
    }
}
