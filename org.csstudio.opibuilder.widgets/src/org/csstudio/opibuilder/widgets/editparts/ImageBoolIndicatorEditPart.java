/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgets.FigureTransparencyHelper;
import org.csstudio.opibuilder.widgets.figures.ImageBoolButtonFigure;
import org.csstudio.opibuilder.widgets.model.ImageBoolIndicatorModel;
import org.csstudio.swt.widgets.symbol.SymbolImageProperties;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * EditPart controller for the image widget.
 */
public final class ImageBoolIndicatorEditPart extends AbstractBoolEditPart {

    private int maxAttempts;

    @Override
    public ImageBoolIndicatorModel getWidgetModel() {
        return (ImageBoolIndicatorModel) getModel();
    }

    @Override
    protected IFigure doCreateFigure() {
        ImageBoolIndicatorModel model = getWidgetModel();
        // create AND initialize the view properly
        final ImageBoolButtonFigure figure = new ImageBoolButtonFigure(true);
        initializeCommonFigureProperties(figure, model);

        SymbolImageProperties sip = new SymbolImageProperties();
        sip.setStretch(model.isStretch());
        sip.setAutoSize(model.isAutoSize());
        sip.setAnimationDisabled(model.isStopAnimation());
        sip.setAlignedToNearestSecond(model.isAlignedToNearestSecond());
        sip.setBackgroundColor(new Color(Display.getDefault(), model.getBackgroundColor()));
        figure.setSymbolProperties(sip, model);
        figure.setImageLoadedListener(figure1 -> {
            ImageBoolButtonFigure symbolFigure = (ImageBoolButtonFigure) figure1;
            autoSizeWidget(symbolFigure);
        });
        figure.setOnImagePath(model.getOnImagePath());
        figure.setOffImagePath(model.getOffImagePath());
        return figure;
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        // Don't autosize to save CPU usage
        // removeAllPropertyChangeHandlers(AbstractPVWidgetModel.PROP_PVVALUE);
        // value
        // IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {
        // public boolean handleChange(final Object oldValue,
        // final Object newValue,
        // final IFigure refreshableFigure) {
        // if(newValue == null)
        // return false;
        // ImageBoolButtonFigure figure = (ImageBoolButtonFigure) refreshableFigure;
        //// autoSizeWidget(figure);
        // return true;
        // }
        // };
        // setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVVALUE, handler);

        // changes to the on image property
        IWidgetPropertyChangeHandler handle = (oldValue, newValue, figure) -> {
            ImageBoolButtonFigure imageFigure = (ImageBoolButtonFigure) figure;
            String absolutePath = (String) newValue;
            if (!absolutePath.contains("://")) {
                IPath path = Path.fromPortableString(absolutePath);
                if (!path.isAbsolute()) {
                    path = ResourceUtil.buildAbsolutePath(getWidgetModel(), path);
                    absolutePath = path.toPortableString();
                }
            }

            imageFigure.setOnImagePath(absolutePath);
            autoSizeWidget(imageFigure);
            return true;
        };
        setPropertyChangeHandler(ImageBoolIndicatorModel.PROP_ON_IMAGE, handle);

        // changes to the off image property
        handle = (oldValue, newValue, figure) -> {
            ImageBoolButtonFigure imageFigure = (ImageBoolButtonFigure) figure;
            String absolutePath = (String) newValue;
            if (!absolutePath.contains("://")) {
                IPath path = Path.fromPortableString(absolutePath);
                if (!path.isAbsolute()) {
                    path = ResourceUtil.buildAbsolutePath(getWidgetModel(), path);
                    absolutePath = path.toPortableString();
                }
            }

            imageFigure.setOffImagePath(absolutePath);
            autoSizeWidget(imageFigure);
            return true;
        };
        setPropertyChangeHandler(ImageBoolIndicatorModel.PROP_OFF_IMAGE, handle);

        // changes to the stretch property
        handle = (oldValue, newValue, figure) -> {
            ImageBoolButtonFigure imageFigure = (ImageBoolButtonFigure) figure;
            imageFigure.setStretch((Boolean) newValue);
            autoSizeWidget(imageFigure);
            return true;
        };
        setPropertyChangeHandler(ImageBoolIndicatorModel.PROP_STRETCH, handle);

        // changes to the autosize property
        handle = (oldValue, newValue, figure) -> {
            ImageBoolButtonFigure imageFigure = (ImageBoolButtonFigure) figure;
            autoSizeWidget(imageFigure);
            return true;
        };
        setPropertyChangeHandler(ImageBoolIndicatorModel.PROP_AUTOSIZE, handle);

        // changes to the stop animation property
        handle = (oldValue, newValue, figure) -> {
            ImageBoolButtonFigure imageFigure = (ImageBoolButtonFigure) figure;
            imageFigure.setAnimationDisabled((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(ImageBoolIndicatorModel.PROP_NO_ANIMATION, handle);

        // changes to the align to nearest second property
        handle = (oldValue, newValue, figure) -> {
            ImageBoolButtonFigure imageFigure = (ImageBoolButtonFigure) figure;
            imageFigure.setAlignedToNearestSecond((Boolean) newValue);
            return false;
        };
        setPropertyChangeHandler(ImageBoolIndicatorModel.PROP_ALIGN_TO_NEAREST_SECOND, handle);

        // changes to the border width property
        handle = (oldValue, newValue, figure) -> {
            ImageBoolButtonFigure imageFigure = (ImageBoolButtonFigure) figure;
            autoSizeWidget(imageFigure);
            return true;
        };
        setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_WIDTH, handle);
        setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_STYLE, handle);

        // size change handlers - so we can stretch accordingly
        handle = (oldValue, newValue, figure) -> {
            ImageBoolButtonFigure imageFigure = (ImageBoolButtonFigure) figure;
            autoSizeWidget(imageFigure);
            return true;
        };
        setPropertyChangeHandler(AbstractWidgetModel.PROP_HEIGHT, handle);
        setPropertyChangeHandler(AbstractWidgetModel.PROP_WIDTH, handle);

        FigureTransparencyHelper.addHandler(this, figure);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ((ImageBoolButtonFigure) getFigure()).dispose();
    }

    private void autoSizeWidget(ImageBoolButtonFigure imageFigure) {
        if (!getWidgetModel().isAutoSize()) {
            return;
        }
        maxAttempts = 10;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (maxAttempts-- > 0 && imageFigure.isLoadingImage()) {
                    Display.getDefault().timerExec(100, this);
                    return;
                }
                ImageBoolIndicatorModel model = getWidgetModel();
                Dimension d = imageFigure.getAutoSizedDimension();
                if (model.isAutoSize() && !model.isStretch() && d != null) {
                    model.setSize(d.width, d.height);
                }

            }
        };
        Display.getDefault().timerExec(100, task);
    }
}
