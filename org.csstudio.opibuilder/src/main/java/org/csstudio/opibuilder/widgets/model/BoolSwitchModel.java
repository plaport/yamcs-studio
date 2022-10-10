/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.properties.ActionsProperty;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.swt.graphics.RGB;

/**
 * The widget model for Boolean Switch.
 */
public class BoolSwitchModel extends AbstractBoolControlModel {

    /**
     * True if the widget is drawn with advanced graphics. In some platforms, advance graphics may not be available, in
     * which case the widget will not be drawn with advanced graphics even this is set to true.
     */
    public static final String PROP_EFFECT3D = "effect_3d";

    /** The default value of the height property. */
    private static final int DEFAULT_HEIGHT = 100;

    /** The default value of the width property. */
    private static final int DEFAULT_WIDTH = 50;

    private static final RGB DEFAULT_FORE_COLOR = CustomMediaFactory.COLOR_BLACK;

    public BoolSwitchModel() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setForegroundColor(DEFAULT_FORE_COLOR);
    }

    @Override
    protected void configureProperties() {
        super.configureProperties();

        addProperty(new BooleanProperty(PROP_EFFECT3D, "3D Effect", WidgetPropertyCategory.Display, true));
        removeProperty(PROP_ACTIONS);
        addProperty(new ActionsProperty(PROP_ACTIONS, "Actions", WidgetPropertyCategory.Behavior, false));
        // setPropertyDescription(PROP_PVNAME, "Readback PV");
    }

    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.BoolSwitch";

    @Override
    public String getTypeID() {
        return ID;
    }

    /**
     * @return true if the widget would be painted with 3D effect, false otherwise
     */
    public boolean isEffect3D() {
        return (Boolean) getProperty(PROP_EFFECT3D).getPropertyValue();
    }
}
