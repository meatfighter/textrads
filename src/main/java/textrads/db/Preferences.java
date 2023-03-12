package textrads.db;

import java.io.Serializable;

public class Preferences implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final byte marathonLevel;
    
    private final byte constantLevelLevel;
    
    private final byte garbageHeapLevel;
    private final byte garbageHeapHeight;
    
    private final byte risingGarbageLevel;
    
    private final byte threeMinutesLevel;
    
    private final byte fortyLinesLevel;
    private final byte fortyLinesHeight;
    
    private final byte noRotationLevel;
    
    private final byte invisibleLevel;
    
    private final byte vsAiLevel;
    private final byte vsAiDifficulty;
    
    private final byte vsHumanLevel;
    
    private final String initials;
    
    public Preferences() {
        this((byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, 
                (byte) -1, (byte) -1, (byte) -1, (byte) -1, null);
    }

    private Preferences(
            final byte marathonLevel, 
            final byte constantLevelLevel, 
            final byte garbageHeapLevel, 
            final byte garbageHeapHeight, 
            final byte risingGarbageLevel, 
            final byte threeMinutesLevel, 
            final byte fortyLinesLevel, 
            final byte fortyLinesHeight, 
            final byte noRotationLevel, 
            final byte invisibleLevel, 
            final byte vsAiLevel, 
            final byte vsAiDifficulty, 
            final byte vsHumanLevel, 
            final String initials) {
        
        this.marathonLevel = marathonLevel;
        this.constantLevelLevel = constantLevelLevel;
        this.garbageHeapLevel = garbageHeapLevel;
        this.garbageHeapHeight = garbageHeapHeight;
        this.risingGarbageLevel = risingGarbageLevel;
        this.threeMinutesLevel = threeMinutesLevel;
        this.fortyLinesLevel = fortyLinesLevel;
        this.fortyLinesHeight = fortyLinesHeight;
        this.noRotationLevel = noRotationLevel;
        this.invisibleLevel = invisibleLevel;
        this.vsAiLevel = vsAiLevel;
        this.vsAiDifficulty = vsAiDifficulty;
        this.vsHumanLevel = vsHumanLevel;
        this.initials = initials;
    }

    public byte getMarathonLevel() {
        return marathonLevel;
    }
    
    public Preferences setMarathonLevel(final byte marathonLevel) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }

    public byte getConstantLevelLevel() {
        return constantLevelLevel;
    }
    
    public Preferences setConstantLevelLevel(final byte constantLevelLevel) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public byte getGarbageHeapLevel() {
        return garbageHeapLevel;
    }
    
    public Preferences setGarbageHeapLevel(final byte garbageHeapLevel) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public byte getGarbageHeapHeight() {
        return garbageHeapHeight;
    }
    
    public Preferences setGarbageHeapHeight(final byte garbageHeapHeight) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public byte getRisingGarbageLevel() {
        return risingGarbageLevel;
    }
    
    public Preferences setRisingGarbageLevel(final byte risingGarbageLevel) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public byte getThreeMinutesLevel() {
        return threeMinutesLevel;
    }
    
    public Preferences setThreeMinutesLevel(final byte threeMinutesLevel) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public byte getFortyLinesLevel() {
        return fortyLinesLevel;
    }
    
    public Preferences setFortyLinesLevel(final byte fortyLinesLevel) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public byte getFortyLinesHeight() {
        return fortyLinesHeight;
    }
    
    public Preferences setFortyLinesHeight(final byte fortyLinesHeight) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public byte getNoRotationLevel() {
        return noRotationLevel;
    }
    
    public Preferences setNoRotationLevel(final byte noRotationLevel) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public byte getInvisibleLevel() {
        return invisibleLevel;
    }
    
    public Preferences setInvisibleLevel(final byte invisibleLevel) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public byte getVsAiLevel() {
        return vsAiLevel;
    }
    
    public Preferences setVsAiLevel(final byte vsAiLevel) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public byte getVsAiDifficulty() {
        return vsAiDifficulty;
    }
    
    public Preferences setVsAiDifficulty(final byte vsAiDifficulty) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public byte getVsHumanLevel() {
        return vsHumanLevel;
    }
    
    public Preferences setVsHumanLevel(final byte vsHumanLevel) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    

    public String getInitials() {
        return initials;
    }
    
    public Preferences setInitials(final String initials) {
        return new Preferences(marathonLevel, constantLevelLevel, garbageHeapLevel, garbageHeapHeight, 
                risingGarbageLevel, threeMinutesLevel, fortyLinesLevel, fortyLinesHeight, noRotationLevel, 
                invisibleLevel, vsAiLevel, vsAiDifficulty, vsHumanLevel, initials);
    }    
}