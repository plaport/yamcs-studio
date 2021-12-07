/********************************************************************************
 * Copyright (c) 2006 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.commands.CreateGuideCommand;
import org.csstudio.opibuilder.commands.DeleteGuideCommand;
import org.csstudio.opibuilder.commands.MoveGuideCommand;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.GuideModel;
import org.csstudio.opibuilder.model.RulerModel;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.rulers.RulerChangeListener;
import org.eclipse.gef.rulers.RulerProvider;

/**
 * The RulerProvider for the OPI Editor.
 */
public final class OPIEditorRulerProvider extends RulerProvider {

    /**
     * A PropertyChangeListener for rulers.
     */
    private PropertyChangeListener rulerListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(RulerModel.PROPERTY_CHILDREN_CHANGED)) {
                var guide = (GuideModel) evt.getNewValue();
                if (getGuides().contains(guide)) {
                    guide.addPropertyChangeListener(guideListener);
                } else {
                    guide.removePropertyChangeListener(guideListener);
                }
                for (var i = 0; i < listeners.size(); i++) {
                    ((RulerChangeListener) listeners.get(i)).notifyGuideReparented(guide);
                }
            }
        }
    };

    /**
     * A PropertyChangeListener for guides.
     */
    private PropertyChangeListener guideListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(GuideModel.PROPERTY_CHILDREN_CHANGED)) {
                for (var i = 0; i < listeners.size(); i++) {
                    ((RulerChangeListener) listeners.get(i)).notifyPartAttachmentChanged(evt.getNewValue(),
                            evt.getSource());
                }
            } else {
                for (var i = 0; i < listeners.size(); i++) {
                    ((RulerChangeListener) listeners.get(i)).notifyGuideMoved(evt.getSource());
                }
            }
        }
    };

    /**
     * The Model for the rulers.
     */
    private RulerModel ruler;

    /**
     * Constructor.
     * 
     * @param ruler
     *            The RulerModel fore this provider
     */
    public OPIEditorRulerProvider(RulerModel ruler) {
        this.ruler = ruler;
        this.ruler.addPropertyChangeListener(rulerListener);
        var guides = getGuides();
        for (var i = 0; i < guides.size(); i++) {
            ((GuideModel) guides.get(i)).addPropertyChangeListener(guideListener);
        }
    }

    @Override
    public Object getRuler() {
        return ruler;
    }

    @Override
    public int getUnit() {
        return RulerProvider.UNIT_PIXELS;
    }

    @Override
    public List<AbstractWidgetModel> getAttachedModelObjects(Object guide) {
        return new ArrayList<AbstractWidgetModel>(((GuideModel) guide).getAttachedModels());
    }

    @Override
    public Command getMoveGuideCommand(Object guide, int pDelta) {
        return new MoveGuideCommand((GuideModel) guide, pDelta);
    }

    @Override
    public Command getCreateGuideCommand(int position) {
        return new CreateGuideCommand(ruler, position);
    }

    @Override
    public Command getDeleteGuideCommand(Object guide) {
        return new DeleteGuideCommand((GuideModel) guide, ruler);
    }

    @Override
    public int[] getGuidePositions() {
        var guides = getGuides();
        var result = new int[guides.size()];
        for (var i = 0; i < guides.size(); i++) {
            result[i] = ((GuideModel) guides.get(i)).getPosition();
        }
        return result;
    }

    @Override
    public int getGuidePosition(Object guide) {
        return ((GuideModel) guide).getPosition();
    }

    @Override
    public List<GuideModel> getGuides() {
        return ruler.getGuides();
    }
}
