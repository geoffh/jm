package jmusic.oldneedsrewrite.util;

public class VolumeRange {
    public Integer minValue;
    public Integer maxValue;

    public VolumeRange( Integer minValue, Integer maxValue ) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public Integer getMinValue() {
            return minValue;
        }
    public Integer getMaxValue() { return maxValue; }
}
