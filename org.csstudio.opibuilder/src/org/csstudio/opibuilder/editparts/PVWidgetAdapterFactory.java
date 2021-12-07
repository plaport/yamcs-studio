package org.csstudio.opibuilder.editparts;

import org.csstudio.csdata.ProcessVariable;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * The adaptor factory to make a PV widget as a PV provider for css context menu.
 */
public class PVWidgetAdapterFactory implements IAdapterFactory {

    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adaptableObject instanceof IPVWidgetEditpart) {
            if (adapterType == ProcessVariable.class) {
                return adapterType.cast(new ProcessVariable(((IPVWidgetEditpart) adaptableObject).getPVName()));
            } else if (adapterType == ProcessVariable[].class) {
                var allPVNames = ((IPVWidgetEditpart) adaptableObject).getAllPVNames();
                var pvs = new ProcessVariable[allPVNames.length];
                var i = 0;
                for (String s : allPVNames) {
                    pvs[i++] = new ProcessVariable(s);
                }
                return adapterType.cast(pvs);
            }

        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class<?>[] { ProcessVariable.class, ProcessVariable[].class };
    }

}
