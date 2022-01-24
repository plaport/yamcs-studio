/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.util;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.GuideModel;

/**
 * A class, which contains the information which {@link AbstractWidgetModel} is attached to which vertical and
 * horizontal Guide.
 *
 */
public final class GuideUtil {
    /**
     * A Map which contains the vertical guide of the AbstractWidgetModels.
     */
    private Map<AbstractWidgetModel, GuideModel> _verticalGuides = new HashMap<>();
    /**
     * A Map which contains the horizontal guide of the AbstractWidgetModels.
     */
    private Map<AbstractWidgetModel, GuideModel> _horizontalGuides = new HashMap<>();

    /**
     * The instance of the GuideUtil.
     */
    private static GuideUtil _instance;

    /**
     * Constructor.
     */
    private GuideUtil() {
        // nothing to do yet
    }

    /**
     * Returns the instance of the GuideUtil.
     * 
     * @return GuideUtil The GuideUtil
     */
    public static GuideUtil getInstance() {
        if (_instance == null) {
            _instance = new GuideUtil();
        }
        return _instance;
    }

    /**
     * Sets the Guide for the given {@link AbstractWidgetModel}. Regarding if the guide is vertical or horizontal
     * 
     * @param model
     *            The AbstractWidgetModel, which is attached to the guide
     * @param guide
     *            The guide (may not be null)
     */
    public void setGuide(AbstractWidgetModel model, GuideModel guide) {
        assert guide != null;
        if (guide.isHorizontal()) {
            // if the is already attached to another guide, cancel this connection
            if (_horizontalGuides.containsKey(model)) {
                if (guide != _horizontalGuides.get(model)) {
                    _horizontalGuides.get(model).detachPart(model);
                }
            }
            _horizontalGuides.put(model, guide);
        } else {
            // if the is already attached to another guide, cancel this connection
            if (_verticalGuides.containsKey(model)) {
                if (guide != _verticalGuides.get(model)) {
                    _verticalGuides.get(model).detachPart(model);
                }
            }
            _verticalGuides.put(model, guide);
        }
    }

    /**
     * Removes the vertical or the horizontal Guide of a {@link AbstractWidgetModel} depending on the boolean flag.
     * 
     * @param model
     *            The AbstractWidgetModel, which is attached to the guide
     * @param horizontal
     *            The orientation of the guide, which should be removed
     */
    public void removeGuide(AbstractWidgetModel model, boolean horizontal) {
        if (horizontal) {
            _horizontalGuides.remove(model);
        } else {
            _verticalGuides.remove(model);
        }
    }

    /**
     * Returns the guide to which the given {@link AbstractWidgetModel} is attached in the given orientation.
     * 
     * @param model
     *            The AbstractWidgetModel, which is attached to the guide
     * @param horizontal
     *            The orientation of the guide, which should be returned
     * @return GuideModel The guide to which the given {@link AbstractWidgetModel} is attached in the given orientation
     *         or null.
     */
    public GuideModel getGuide(AbstractWidgetModel model, boolean horizontal) {
        if (horizontal) {
            return _horizontalGuides.get(model);
        }
        return _verticalGuides.get(model);
    }
}
