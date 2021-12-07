/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VInt;

public class Sint32VType extends YamcsVType implements VInt {

    public Sint32VType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Integer getValue() {
        return value.getSint32Value();
    }

    @Override
    public String toString() {
        return String.valueOf(value.getSint32Value());
    }
}
