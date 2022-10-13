/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.script;

/**
 * A store to save script related information and register or unregister script for PVs input.
 */
public interface IScriptStore {

    /**
     * Remove listeners from PV. Dispose related resource if needed.
     */
    void unRegister();
}