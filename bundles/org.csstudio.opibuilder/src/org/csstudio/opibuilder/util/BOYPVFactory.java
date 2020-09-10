package org.csstudio.opibuilder.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.yamcs.studio.data.ExceptionHandler;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.PVFactory;

/**
 * The factory to create a PV for BOY. It will create either Utility PV or PVManager PV which depends on the preference
 * settings.
 */
public class BOYPVFactory {

    /**
     * The default background thread for PV change event notification.
     */
    private final static ExecutorService BOY_PV_THREAD = Executors
            .newSingleThreadExecutor();

    private final static ExceptionHandler exceptionHandler = ex -> ErrorHandlerUtil
            .handleError("Error from pv connection layer: ", ex);

    /**
     * Create a PV. If it is using PV Manager, buffered all values is false and max update rate is determined by GUI
     * Refresh cycle.
     * 
     * @param name
     *            name of the PV.
     * @return the PV
     * @throws Exception
     * @see {@link #createPV(String, boolean, int)}
     */
    public static IPV createPV(String name) throws Exception {
        return createPV(name, false);
    }

    /**
     * Create a PV. If it is using PV Manager, max update rate is determined by GUI Refresh cycle.
     * 
     * @param name
     *            name of the PV.
     * @param bufferAllValues
     *            if all values should be buffered. Only meaningful if it is using PV Manager.
     * @return the PV
     * @throws Exception
     * @see {@link #createPV(String, boolean, int)}
     */
    public static IPV createPV(String name, boolean bufferAllValues) throws Exception {
        return createPV(name, bufferAllValues, PreferencesHelper.getGUIRefreshCycle());
    }

    /**
     * Create a PV based on PV connection layer preference.
     * 
     * @param name
     *            name of the PV.
     * @param bufferAllValues
     *            if all values should be buffered. Only meaningful if it is using PV Manager.
     * @param updateDuration
     *            the fastest update duration.
     * @return the PV
     * @throws Exception
     */
    public static IPV createPV(String name, boolean bufferAllValues, int updateDuration) throws Exception {
        PVFactory pvFactory = PVFactory.getInstance();
        return pvFactory.createPV(
                name, false, updateDuration, bufferAllValues, BOY_PV_THREAD, exceptionHandler);
    }
}
