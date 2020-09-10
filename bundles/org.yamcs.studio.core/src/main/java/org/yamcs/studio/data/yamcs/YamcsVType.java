package org.yamcs.studio.data.yamcs;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.List;

import org.yamcs.protobuf.Mdb.AlarmLevelType;
import org.yamcs.protobuf.Mdb.AlarmRange;
import org.yamcs.protobuf.Pvalue.AcquisitionStatus;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.core.MissionDatabase;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.vtype.Alarm;
import org.yamcs.studio.data.vtype.AlarmSeverity;
import org.yamcs.studio.data.vtype.Display;
import org.yamcs.studio.data.vtype.NumberFormats;
import org.yamcs.studio.data.vtype.Time;
import org.yamcs.studio.data.vtype.VType;

public class YamcsVType implements VType, Alarm, Time, Display {

    public static final YamcsVType NO_VALUE = new YamcsVType(null);

    protected ParameterValue pval;

    public YamcsVType(ParameterValue pval) {
        this.pval = pval;
    }

    @Override
    public AlarmSeverity getAlarmSeverity() {
        if (pval == null) {
            return AlarmSeverity.NONE;
        }

        if (pval.getAcquisitionStatus() == AcquisitionStatus.EXPIRED
                || pval.getAcquisitionStatus() == AcquisitionStatus.NOT_RECEIVED
                || pval.getAcquisitionStatus() == AcquisitionStatus.INVALID) {
            return AlarmSeverity.INVALID; // Workaround to display LOS in the displays, should be 'Expired'
        }

        if (!pval.hasMonitoringResult()) {
            return AlarmSeverity.NONE;
        }

        switch (pval.getMonitoringResult()) {
        case IN_LIMITS:
            return AlarmSeverity.NONE;
        case DISABLED:
            return AlarmSeverity.NONE;
        case WATCH:
        case WARNING:
        case DISTRESS:
            return AlarmSeverity.MINOR;
        case CRITICAL:
        case SEVERE:
            return AlarmSeverity.MAJOR;
        default:
            throw new IllegalStateException("Unexpected alarm severity " + pval.getMonitoringResult());
        }
    }

    @Override
    public String getAlarmName() {
        return "";
    }

    @Override
    public Instant getTimestamp() {
        if (pval != null && pval.hasGenerationTime()) {
            return Instant.ofEpochSecond(pval.getGenerationTime().getSeconds(), pval.getGenerationTime().getNanos());
        } else {
            return null;
        }
    }

    @Override
    public Integer getTimeUserTag() {
        return null;
    }

    @Override
    public boolean isTimeValid() {
        return true;
    }

    @Override
    public Double getLowerWarningLimit() {
        if (pval != null) {
            // Assumes ordered ranges
            for (AlarmRange range : pval.getAlarmRangeList()) {
                if (range.getLevel() == AlarmLevelType.WATCH
                        || range.getLevel() == AlarmLevelType.WARNING
                        || range.getLevel() == AlarmLevelType.DISTRESS) {
                    if (range.hasMinInclusive()) {
                        return range.getMinInclusive();
                    } else if (range.hasMinExclusive()) {
                        return range.getMinExclusive();
                    }
                }
            }
        }
        return Double.NaN;
    }

    /**
     * Highest value before the warning region
     */
    @Override
    public Double getUpperWarningLimit() {
        if (pval != null) {
            // Assumes ordered ranges
            for (AlarmRange range : pval.getAlarmRangeList()) {
                if (range.getLevel() == AlarmLevelType.WATCH
                        || range.getLevel() == AlarmLevelType.WARNING
                        || range.getLevel() == AlarmLevelType.DISTRESS) {
                    if (range.hasMaxInclusive()) {
                        return range.getMaxInclusive();
                    } else if (range.hasMaxExclusive()) {
                        return range.getMaxExclusive();
                    }
                }
            }
        }
        return Double.NaN;
    }

    @Override
    public Double getLowerAlarmLimit() {
        if (pval != null) {
            // Assumes ordered ranges
            for (AlarmRange range : pval.getAlarmRangeList()) {
                if (range.getLevel() == AlarmLevelType.CRITICAL
                        || range.getLevel() == AlarmLevelType.SEVERE) {
                    if (range.hasMinInclusive()) {
                        return range.getMinInclusive();
                    } else if (range.hasMinExclusive()) {
                        return range.getMinExclusive();
                    }
                }
            }
        }
        return Double.NaN;
    }

    /**
     * Highest value before the alarm region
     */
    @Override
    public Double getUpperAlarmLimit() {
        if (pval != null) {
            // Assumes ordered ranges
            for (AlarmRange range : pval.getAlarmRangeList()) {
                if (range.getLevel() == AlarmLevelType.CRITICAL
                        || range.getLevel() == AlarmLevelType.SEVERE) {
                    if (range.hasMaxInclusive()) {
                        return range.getMaxInclusive();
                    } else if (range.hasMaxExclusive()) {
                        return range.getMaxExclusive();
                    }
                }
            }
        }
        return Double.NaN;
    }

    @Override
    public Double getLowerDisplayLimit() {
        Double loLimit = getLowerAlarmLimit();
        if (loLimit == Double.NaN) {
            loLimit = getLowerWarningLimit();
        }

        return loLimit;
    }

    @Override
    public Double getUpperDisplayLimit() {
        Double hiLimit = getUpperAlarmLimit();
        if (hiLimit == Double.NaN) {
            hiLimit = getUpperWarningLimit();
        }

        return hiLimit;
    }

    @Override
    public Double getLowerCtrlLimit() {
        return Double.NaN;
    }

    @Override
    public Double getUpperCtrlLimit() {
        return Double.NaN;
    }

    @Override
    public String getUnits() {
        if (pval != null) {
            MissionDatabase mdb = YamcsPlugin.getMissionDatabase();
            if (mdb != null) {
                String unit = mdb.getCombinedUnit(pval.getId());
                return (unit == null) ? "" : unit;
            }
        }
        return "";
    }

    @Override
    public NumberFormat getFormat() {
        return NumberFormats.toStringFormat();
    }

    /**
     * Converts a yamcs ParameterValue to a VType.
     */
    public static YamcsVType fromYamcs(ParameterValue pval) {
        if (!pval.hasEngValue()) {
            return null;
        }

        Value engValue = pval.getEngValue();
        switch (engValue.getType()) {
        case UINT32:
            return new Uint32VType(pval);
        case SINT32:
            return new Sint32VType(pval);
        case UINT64:
            return new Uint64VType(pval);
        case SINT64:
            return new Sint64VType(pval);
        case FLOAT:
            return new FloatVType(pval);
        case DOUBLE:
            return new DoubleVType(pval);
        case BOOLEAN:
            return new BooleanVType(pval);
        case STRING:
            return new StringVType(pval);
        case BINARY:
            return new BinaryVType(pval);
        case TIMESTAMP:
            return new TimestampVType(pval);
        case ENUMERATED:
            return new EnumeratedVType(pval);
        case AGGREGATE:
            return new AggregateVType(pval);
        case ARRAY:
            List<Value> arrayValues = engValue.getArrayValueList();
            if (arrayValues.isEmpty()) {
                return null; // TODO
            } else {
                switch (arrayValues.get(0).getType()) {
                case UINT32:
                    return new Uint32ArrayVType(pval);
                case SINT32:
                    return new Sint32ArrayVType(pval);
                case UINT64:
                    return new Uint64ArrayVType(pval);
                case SINT64:
                    return new Sint64ArrayVType(pval);
                case FLOAT:
                    return new FloatArrayVType(pval);
                case DOUBLE:
                    return new DoubleArrayVType(pval);
                case BOOLEAN:
                    return new BooleanArrayVType(pval);
                case STRING:
                    return new StringArrayVType(pval);
                case ENUMERATED:
                    return new EnumeratedArrayVType(pval);
                case AGGREGATE:
                    return new AggregateArrayVType(pval);
                case ARRAY:
                    return new ArrayArrayVType(pval);
                default:
                    throw new IllegalStateException(
                            "Unexpected type for parameter array value. Got: " + arrayValues.get(0).getType());
                }
            }
        default:
            throw new IllegalStateException(
                    "Unexpected type for parameter value. Got: " + engValue.getType());
        }
    }
}
