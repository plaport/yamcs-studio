/********************************************************************************
 * Copyright (c) 2009 Peter Smith and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.parser.engine.expr;

public class ExprExpression extends Expr {

    private Expr child;

    public ExprExpression(Expr child) {
        super(ExprType.Expression);
        this.child = child;
    }

    public Expr getChild() {
        return child;
    }

    public String toString() {
        return "(" + child + ")";
    }

    public void validate() throws ExprException {
        child.validate();
    }
}
