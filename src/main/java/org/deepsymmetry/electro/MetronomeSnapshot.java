package org.deepsymmetry.electro;

/**
 * A basic implementation of the {@link Snapshot} interface supporting multiple coherent computations about a
 * fixed point in time on a metronome's timeline.
 *
 * @author James Elliott
 */
@SuppressWarnings("WeakerAccess")
class MetronomeSnapshot implements Snapshot {

    /**
     * The point in time at which the metronome from which this snapshot was taken started counting.
     */
    private final long startTime;

    /**
     * The number of beats per minute at which the metronome from which this snapshot was taken was running.
     */
    private final double tempo;

    /**
     * The number of beats which made up a bar in the metronome from which this snapshot was taken.
     */
    private final int beatsPerBar;

    /**
     * The number of beats which made up a bar in the metronome from which this snapshot was taken.
     */
    private final int barsPerPhrase;

    /**
     * The point in time with respect to which the snapshot is computed. The difference between this and
     * {@link #startTime}, along with {@link #tempo}, determine the other values.
     */
    private final long instant;

    /**
     * The duration of a beat, in milliseconds, at the tempo when the snapshot was taken.
     */
    private final double beatInterval;

    /**
     * The duration of a bar, in milliseconds, at the tempo when the snapshot was taken.
     */
    private final double barInterval;

    /**
     * The duration of a phrase, in milliseconds, at the tempo when the snapshot was taken.
     */
    private final double phraseInterval;

    /**
     * Create a snapshot of the state of the metronome at the current instant in time.
     *
     * @param metronome the time keeper whose current state is to be analyzed and frozen
     */
    MetronomeSnapshot(Metronome metronome) {
        this(metronome, System.currentTimeMillis());
    }

    /**
     * Create a snapshot of the state of the metronome at a particular instant in time.
     *
     * @param metronome the time keeper whose current state is to be analyzed and frozen
     * @param instant the millisecond timestamp at which the state of the metronome is to be considered
     */
    MetronomeSnapshot (Metronome metronome, long instant) {
        startTime = metronome.getStartTime();
        tempo = metronome.getTempo();
        beatsPerBar = metronome.getBeatsPerBar();
        barsPerPhrase = metronome.getBarsPerPhrase();
        this.instant = instant;
        beatInterval = metronome.getBeatInterval();
        barInterval = metronome.getBarInterval();
        phraseInterval = metronome.getPhraseInterval();
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public double getTempo() {
        return tempo;
    }

    @Override
    public int getBeatsPerBar() {
        return beatsPerBar;
    }

    @Override
    public int getBarsPerPhrase() {
        return barsPerPhrase;
    }

    @Override
    public long getInstant() {
        return instant;
    }

    @Override
    public double getBeatInterval() {
        return beatInterval;
    }

    @Override
    public double getBarInterval() {
        return barInterval;
    }

    @Override
    public double getPhraseInterval() {
        return phraseInterval;
    }

    @Override
    public long getBeat() {
        return Metronome.markerNumber(instant, startTime, beatInterval);
    }

    @Override
    public long getBar() {
        return  Metronome.markerNumber(instant, startTime, barInterval);
    }

    @Override
    public long getPhrase() {
        return Metronome.markerNumber(instant, startTime, phraseInterval);
    }

    @Override
    public double getBeatPhase() {
        return Metronome.markerPhase(instant, startTime, beatInterval);
    }

    @Override
    public double getBarPhase() {
        return Metronome.markerPhase(instant, startTime, barInterval);
    }

    @Override
    public double getPhrasePhase() {
        return Metronome.markerPhase(instant, startTime, phraseInterval);
    }

   @Override
    public long getTimeOfBeat(long beat) {
        return Math.round((beat - 1) * beatInterval) + startTime;
    }

    @Override
    public int getBeatWithinBar() {
        final double beatSize = 1.0 / beatsPerBar;
        return 1 + (int)Math.floor(getBarPhase() / beatSize);
    }

    @Override
    public boolean isDownBeat() {
        return getBeatWithinBar() == 1;
    }

    @Override
    public int getBeatWithinPhrase() {
        final double beatSize = 1.0 / (beatsPerBar * barsPerPhrase);
        return 1 + (int)Math.floor(getPhrasePhase() / beatSize);
    }

    @Override
    public boolean isPhraseStart() {
        return getBeatWithinPhrase() == 1;
    }

    @Override
    public long getTimeOfBar(long bar) {
        return Math.round((bar - 1) * barInterval) + startTime;
    }

    @Override
    public int getBarWithinPhrase() {
        final double barSize = 1.0 / barsPerPhrase;
        return 1 + (int)Math.floor(getPhrasePhase() / barSize);
    }

    @Override
    public long getTimeOfPhrase(long phrase) {
        return Math.round((phrase - 1) * phraseInterval) + startTime;
    }

    @Override
    public String getMarker () {
        return getPhrase() + "." + getBarWithinPhrase() + "." + getBeatWithinBar();
    }

    @Override
    public double distanceFromBeat() {
        final double phaseDistance = Metronome.findClosestDelta(getBeatPhase());
        return phaseDistance * getBeatInterval();
    }

    @Override
    public double distanceFromBar() {
        final double phaseDistance = Metronome.findClosestDelta(getBarPhase());
        return phaseDistance * getBarInterval();
    }

    @Override
    public double distanceFromPhrase() {
        final double phaseDistance = Metronome.findClosestDelta(getPhrasePhase());
        return phaseDistance * getPhraseInterval();
    }

    @Override
    public String toString() {
        return "Snapshot[marker: " + getMarker() + ", startTime:" + startTime +
                " (" + new java.util.Date(startTime) + "), instant: " + instant +
                " (" + new java.util.Date(instant) + "), beatPhase:" + getBeatPhase() +
                ", barPhase:" + getBarPhase() + ", phrasePhase:" + getPhrasePhase() +
                ", tempo:" + tempo + ", beatsPerBar:" + beatsPerBar + ", barsPerPhrase:" + barsPerPhrase +
                ", beatInterval:" + beatInterval + ", barInterval:" + barInterval +
                ", phraseInterval:" + phraseInterval + "]";
    }
}
