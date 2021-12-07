/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2012-18 diirt developers.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.yamcs.studio.data.formula.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import org.yamcs.studio.data.vtype.ListNumberProvider;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Formula function that constructs an array with given data and boundaries.
 */
class ArrayWithBoundariesFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return true;
    }

    @Override
    public String getName() {
        return "arrayWithBoundaries";
    }

    @Override
    public String getDescription() {
        return "Returns an array with the given values and cell boundaries";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumberArray.class, ListNumberProvider.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("dataArray", "boundaries");
    }

    @Override
    public Class<?> getReturnType() {
        return VNumberArray.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        if (containsNull(args)) {
            return null;
        }

        var array = (VNumberArray) args.get(0);
        if (array.getSizes().size() != args.size() - 1) {
            throw new IllegalArgumentException("Dimension of the array must match the number of ListNumberProvider");
        }

        List<ArrayDimensionDisplay> dimDisplay = new ArrayList<>();
        for (var i = 1; i < args.size(); i++) {
            var numberGenerator = (ListNumberProvider) args.get(i);
            dimDisplay.add(
                    ValueFactory.newDisplay(numberGenerator.createListNumber(array.getSizes().getInt(i - 1) + 1), ""));
        }

        return ValueFactory.newVNumberArray(array.getData(), array.getSizes(), dimDisplay, array, array, array);
    }

    private static boolean containsNull(Collection<Object> args) {
        for (Object object : args) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }
}
