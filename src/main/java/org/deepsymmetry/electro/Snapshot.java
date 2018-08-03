package org.deepsymmetry.electro;

/**
 * A snapshot to support a series of beat and phase calculations with respect to a given instant in time,
 * so the calculations can all remain consistent even if they take a while to complete.
 *
 * Snapshots also extend the notions of beat phase to enable oscillators with frequencies that are fractions or
 * multiples of a beat.
 *
 * @author James Elliott
 */
public class Snapshot {

    /**
     * The point in time at which the metronome from which this snapshot was taken started counting.
     */
    public final long startTime;

    /**
     * The number of beats per minute at which the metronome from which this snapshot was taken was running.
     */
    public final double tempo;

    /**
     * The number of beats which made up a bar in the metronome from which this snapshot was taken.
     */
    public final int beatsPerBar;

    /**
     * The number of beats which made up a bar in the metronome from which this snapshot was taken.
     */
    public final int barsPerPhrase;

    /**
     * The point in time with respect to which the snapshot is computed. The difference between this and
     * {@link #startTime}, along with {@link #tempo}, determine the other values.
     */
    public final long instant;

    /**
     * The beat number being played at the time represented by the snapshot. Beats start at 1.
     */
    public final int beat;

    /**
     * The bar number being played at the time represented by the snapshot. Bars start at 1.
     */
    public final int bar;

    /**
     * The phrase number being played at the time represented by the snapshot. Phrases start at 1.
     */
    public final int phrase;

    /**
     * The metronome's beat phase at the time of the snapshot, a value which starts at 0.0 at the very start of the
     * beat, but never quite reaches 1.0, because that would be the start of the next beat.
     */
    public final double beatPhase;

    /**
     * The metronome's bar phase at the time of the snapshot, a value which starts at 0.0 at the very start of the
     * bar, but never quite reaches 1.0, because that would be the start of the next bar.
     */
    public final double barPhase;

    /**
     * The metronome's phrase phase at the time of the snapshot, a value which starts at 0.0 at the very start of the
     * phrase, but never quite reaches 1.0, because that would be the start of the next phrase.
     */
    public final double phrasePhase;



}
