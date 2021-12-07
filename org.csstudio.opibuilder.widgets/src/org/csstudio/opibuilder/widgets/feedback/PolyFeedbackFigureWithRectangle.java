/********************************************************************************
 * Copyright (c) 2008 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.feedback;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.PointList;

/**
 * Custom feedback figure for polyglines. The figure shows a rectangle, which does also include the shape of the
 * polyline.
 *
 */
public final class PolyFeedbackFigureWithRectangle extends RectangleFigure {
    /**
     * The "included" polyline.
     */
    private Polyline _innerFigure;

    /**
     * Constructor.
     *
     * @param polyline
     *            the inner figure (may be a polyline or polygon)
     * @param points
     *            the polygon points
     */
    public PolyFeedbackFigureWithRectangle(final Polyline polyline,
            final PointList points) {
        assert polyline != null;
        _innerFigure = polyline;
        add(_innerFigure);
        setPoints(points);
    }

    /**
     * Gets the point list for the polyline part of this figure.
     *
     * @return a point list
     */
    public PointList getPoints() {
        return _innerFigure.getPoints();
    }

    /**
     * Sets the point list for the polyline part.
     *
     * @param points
     *            the point list
     */
    public void setPoints(final PointList points) {
        _innerFigure.setPoints(points);
        setBounds(points.getBounds());
    }

    @Override
    public void paint(final Graphics graphics) {
        // enable tranparency
        graphics.setAlpha(120);
        super.paint(graphics);
    }
}
