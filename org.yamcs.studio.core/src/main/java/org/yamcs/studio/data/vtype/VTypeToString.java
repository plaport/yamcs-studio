/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.vtype;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Helper class that provides default implementation of toString for VTypes.
 */
public class VTypeToString {

    public static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

    private final static ValueFormat format = new SimpleValueFormat(3);
    private static final DateTimeFormatter timeFormat = TIMESTAMP_FORMAT;

    private static void appendAlarm(StringBuilder builder, Alarm alarm) {
        if (!alarm.getAlarmSeverity().equals(AlarmSeverity.NONE)) {
            builder.append(", ").append(alarm.getAlarmSeverity()).append("(").append(alarm.getAlarmName()).append(")");
        }
    }

    /**
     * Converts the given alarm to a string.
     *
     * @param alarm
     *            the alarm
     * @return the string representation; never null
     */
    public static String alarmToString(Alarm alarm) {
        if (alarm == null) {
            return "NONE";
        }

        return alarm.getAlarmSeverity() + "(" + alarm.getAlarmName() + ")";
    }

    /**
     * Converts the given time to a string.
     *
     * @param time
     *            the time
     * @return the string representation; never null
     */
    public static String timeToString(Time time) {
        if (time == null) {
            return "null";
        }

        return timeFormat.format(ZonedDateTime.ofInstant(time.getTimestamp(), ZoneId.systemDefault())) + "("
                + time.getTimeUserTag() + ")";
    }

    private static void appendTime(StringBuilder builder, Time time) {
        builder.append(", ")
                .append(timeFormat.format(ZonedDateTime.ofInstant(time.getTimestamp(), ZoneId.systemDefault())));
    }

    /**
     * Default toString implementation for VNumber.
     *
     * @param vNumber
     *            the object
     * @return the string representation
     */
    public static String toString(VNumber vNumber) {
        var builder = new StringBuilder();
        Class<?> type = ValueUtil.typeOf(vNumber);
        builder.append(type.getSimpleName()).append('[').append(vNumber.getValue());
        appendAlarm(builder, vNumber);
        appendTime(builder, vNumber);
        builder.append(']');
        return builder.toString();
    }

    /**
     * Default toString implementation for VString.
     *
     * @param vString
     *            the object
     * @return the string representation
     */
    public static String toString(VString vString) {
        var builder = new StringBuilder();
        Class<?> type = ValueUtil.typeOf(vString);
        builder.append(type.getSimpleName()).append("[").append(vString.getValue());
        appendAlarm(builder, vString);
        appendTime(builder, vString);
        builder.append(']');
        return builder.toString();
    }

    /**
     * Default toString implementation for VBoolean.
     *
     * @param vBoolean
     *            the object
     * @return the string representation
     */
    public static String toString(VBoolean vBoolean) {
        var builder = new StringBuilder();
        Class<?> type = ValueUtil.typeOf(vBoolean);
        builder.append(type.getSimpleName()).append("[").append(vBoolean.getValue());
        appendAlarm(builder, vBoolean);
        appendTime(builder, vBoolean);
        builder.append(']');
        return builder.toString();
    }

    /**
     * Default toString implementation for VEnum.
     *
     * @param vEnum
     *            the object
     * @return the string representation
     */
    public static String toString(VEnum vEnum) {
        var builder = new StringBuilder();
        Class<?> type = ValueUtil.typeOf(vEnum);
        builder.append(type.getSimpleName()).append("[").append(vEnum.getValue()).append("(").append(vEnum.getIndex())
                .append(")");
        appendAlarm(builder, vEnum);
        appendTime(builder, vEnum);
        builder.append(']');
        return builder.toString();
    }

    static {
        format.setNumberFormat(NumberFormats.toStringFormat());
    }

    /**
     * Default toString implementation for VNumberArray.
     *
     * @param vNumberArray
     *            the object
     * @return the string representation
     */
    public static String toString(VNumberArray vNumberArray) {
        var builder = new StringBuilder();
        Class<?> type = ValueUtil.typeOf(vNumberArray);
        builder.append(type.getSimpleName()).append("[");
        builder.append(format.format(vNumberArray));
        builder.append(", size ").append(vNumberArray.getData().size());
        appendAlarm(builder, vNumberArray);
        appendTime(builder, vNumberArray);
        builder.append(']');
        return builder.toString();
    }

    /**
     * Default toString implementation for VStringArray.
     *
     * @param vStringArray
     *            the object
     * @return the string representation
     */
    public static String toString(VStringArray vStringArray) {
        var builder = new StringBuilder();
        Class<?> type = ValueUtil.typeOf(vStringArray);
        builder.append(type.getSimpleName()).append("[");
        builder.append(format.format(vStringArray));
        builder.append(", size ").append(vStringArray.getData().size());
        appendAlarm(builder, vStringArray);
        appendTime(builder, vStringArray);
        builder.append(']');
        return builder.toString();
    }

    /**
     * Default toString implementation for VBooleanArray.
     *
     * @param vBooleanArray
     *            the object
     * @return the string representation
     */
    public static String toString(VBooleanArray vBooleanArray) {
        var builder = new StringBuilder();
        Class<?> type = ValueUtil.typeOf(vBooleanArray);
        builder.append(type.getSimpleName()).append("[");
        builder.append(format.format(vBooleanArray));
        builder.append(", size ").append(vBooleanArray.getData().size());
        appendAlarm(builder, vBooleanArray);
        appendTime(builder, vBooleanArray);
        builder.append(']');
        return builder.toString();
    }

    /**
     * Default toString implementation for VEnumArray.
     *
     * @param vEnumArray
     *            the object
     * @return the string representation
     */
    public static String toString(VEnumArray vEnumArray) {
        var builder = new StringBuilder();
        Class<?> type = ValueUtil.typeOf(vEnumArray);
        builder.append(type.getSimpleName()).append("[");
        builder.append(format.format(vEnumArray));
        builder.append(", size ").append(vEnumArray.getData().size());
        appendAlarm(builder, vEnumArray);
        appendTime(builder, vEnumArray);
        builder.append(']');
        return builder.toString();
    }

    /**
     * Default toString implementation for VTable.
     *
     * @param vTable
     *            the object
     * @return the string representation
     */
    public static String toString(VTable vTable) {
        var builder = new StringBuilder();
        builder.append("VTable").append("[").append(vTable.getColumnCount()).append("x").append(vTable.getRowCount())
                .append(", ");
        builder.append(format.format(ValueFactory.newVStringArray(VTableFactory.columnNames(vTable),
                ValueFactory.alarmNone(), ValueFactory.timeNow())));
        builder.append(']');
        return builder.toString();
    }
}
