package textrads.db;

import java.io.Serializable;

public class Preferences implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private byte marathonLevel = -1;
    
    private byte constantLevelLevel = -1;
    
    private byte garbageHeapLevel = -1;
    private byte garbageHeapHeight = -1;
    
    private byte risingGarbageLevel = -1;
    
    private byte threeMinutesLevel = -1;
    
    private byte fortyLinesLevel = -1;
    private byte fortyLinesHeight = -1;
    
    private byte noRotationLevel = -1;
    
    private byte invisibleLevel = -1;
    
    private byte vsAiLevel = -1;
    private byte vsAiDifficulty = -1;
    
    private byte vsHumanLevel = -1;
    
    private String initials;

    public byte getMarathonLevel() {
        return marathonLevel;
    }

    public void setMarathonLevel(final byte marathonLevel) {
        this.marathonLevel = marathonLevel;
    }

    public byte getConstantLevelLevel() {
        return constantLevelLevel;
    }

    public void setConstantLevelLevel(final byte constantLevelLevel) {
        this.constantLevelLevel = constantLevelLevel;
    }

    public byte getGarbageHeapLevel() {
        return garbageHeapLevel;
    }

    public void setGarbageHeapLevel(final byte garbageHeapLevel) {
        this.garbageHeapLevel = garbageHeapLevel;
    }

    public byte getGarbageHeapHeight() {
        return garbageHeapHeight;
    }

    public void setGarbageHeapHeight(final byte garbageHeapHeight) {
        this.garbageHeapHeight = garbageHeapHeight;
    }

    public byte getRisingGarbageLevel() {
        return risingGarbageLevel;
    }

    public void setRisingGarbageLevel(final byte risingGarbageLevel) {
        this.risingGarbageLevel = risingGarbageLevel;
    }

    public byte getThreeMinutesLevel() {
        return threeMinutesLevel;
    }

    public void setThreeMinutesLevel(final byte threeMinutesLevel) {
        this.threeMinutesLevel = threeMinutesLevel;
    }

    public byte getFortyLinesLevel() {
        return fortyLinesLevel;
    }

    public void setFortyLinesLevel(final byte fortyLinesLevel) {
        this.fortyLinesLevel = fortyLinesLevel;
    }

    public byte getFortyLinesHeight() {
        return fortyLinesHeight;
    }

    public void setFortyLinesHeight(final byte fortyLinesHeight) {
        this.fortyLinesHeight = fortyLinesHeight;
    }

    public byte getNoRotationLevel() {
        return noRotationLevel;
    }

    public void setNoRotationLevel(final byte noRotationLevel) {
        this.noRotationLevel = noRotationLevel;
    }

    public byte getInvisibleLevel() {
        return invisibleLevel;
    }

    public void setInvisibleLevel(final byte invisibleLevel) {
        this.invisibleLevel = invisibleLevel;
    }

    public byte getVsAiLevel() {
        return vsAiLevel;
    }

    public void setVsAiLevel(final byte vsAiLevel) {
        this.vsAiLevel = vsAiLevel;
    }

    public byte getVsAiDifficulty() {
        return vsAiDifficulty;
    }

    public void setVsAiDifficulty(final byte vsAiDifficulty) {
        this.vsAiDifficulty = vsAiDifficulty;
    }

    public byte getVsHumanLevel() {
        return vsHumanLevel;
    }

    public void setVsHumanLevel(final byte vsHumanLevel) {
        this.vsHumanLevel = vsHumanLevel;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(final String initials) {
        this.initials = initials;
    }
}
