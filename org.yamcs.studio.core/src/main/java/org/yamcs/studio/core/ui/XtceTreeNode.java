/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui;

/**
 * Can be either an XTCE item or an XTCE subsystem
 */
public interface XtceTreeNode<T> {

    public String getName();

    public XtceTreeNode<T> getParent();
}
