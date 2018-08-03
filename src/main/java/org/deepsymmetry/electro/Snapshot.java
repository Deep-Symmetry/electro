package org.deepsymmetry.electro;

/**
 * A snapshot to support a series of beat and phase calculations with respect to a given instant in time,
 * so the calculations can all remain consistent even if they take a while to complete.
 *
 * Snapshots in Afterglow also extend the notions of beat phase to enable oscillators with frequencies that are
 * fractions or multiples of a beat. Since that is much more difficult in Java than Clojure, and unlikely to be
 * needed outside the context of a lighting controller, it has not yet been ported. Open an issue if you need it!
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
    public final long beat;

    /**
     * The bar number being played at the time represented by the snapshot. Bars start at 1.
     */
    public final long bar;

    /**
     * The phrase number being played at the time represented by the snapshot. Phrases start at 1.
     */
    public final long phrase;

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

    /**
     * Create a snapshot of the state of the metronome at the current instant in time.
     *
     * @param metronome the time keeper whose current state is to be analyzed and frozen
     */
    Snapshot(Metronome metronome) {
        this(metronome, System.currentTimeMillis());
    }

    /**
     * Create a snapshot of the state of the metronome at a particular instant in time.
     *
     * @param metronome the time keeper whose current state is to be analyzed and frozen
     * @param instant the millisecond timestamp at which the state of the metronome is to be considered
     */
    Snapshot (Metronome metronome, long instant) {
        startTime = metronome.getStartTime();
        tempo = metronome.getTempo();
        beatsPerBar = metronome.getBeatsPerBar();
        barsPerPhrase = metronome.getBarsPerPhrase();
        this.instant = instant;
        beat = Metronome.markerNumber(instant, startTime, metronome.getBeatInterval());
        bar = Metronome.markerNumber(instant, startTime, metronome.getBarInterval());
        phrase = Metronome.markerNumber(instant, startTime, metronome.getPhraseInterval());
        beatPhase = Metronome.markerPhase(instant, startTime, metronome.getBeatInterval());
        barPhase = Metronome.markerPhase(instant, startTime, metronome.getBarInterval());
        phrasePhase = Metronome.markerPhase(instant, startTime, metronome.getPhraseInterval());
    }

    /**
     * Return the beat number of the snapshot relative to the start of the bar: the down beat is 1, and the range
     * goes up to the value of {@link #beatsPerBar}.
     *
     * @return the beat number within the current bar being counted
     */
    public int getBeatWithinBar() {
        final double beatSize = 1.0 / beatsPerBar;
        return 1 + (int)Math.floor(barPhase / beatSize);
    }

    /**
     * Checks whether the current beat at the time of the snapshot was the first beat in its bar.
     *
     * @return {@code true} if the snapshot was taken during the first beat of a bar
     */
    public boolean isDownBeat() {
        return getBeatWithinBar() == 1;
    }

    /**
     * Return the beat number of the snapshot relative to the start of the phrase: the phrase starts with beat 1, and
     * the range goes up to the value of {@link #beatsPerBar} times {@link #barsPerPhrase}.
     *
     * @return the beat number within the current phrase being counted
     */
    public int getBeatWithinPhrase() {
        final double beatSize = 1.0 / (beatsPerBar * barsPerPhrase);
        return 1 + (int)Math.floor(phrasePhase / beatSize);
    }

    /**
     * Checks whether the current beat at the time of the snapshot was the first beat in its phrase.
     *
     * @return {@code true} if the snapshot was taken during the first beat of a phrase
     */
    public boolean isPhraseStart() {
        return getBeatWithinPhrase() == 1;
    }

    /**
     * Return the bar number of the snapshot relative to the start of the phrase: the phrase starts with bar 1, and the
     * range goes up to the value of {@link #barsPerPhrase}.
     *
     * @return the bar number within the current phrase being counted
     */
    public int getBarWithinPhrase() {
        final double barSize = 1.0 / barsPerPhrase;
        return 1 + (int)Math.floor(phrasePhase / barSize);
    }

    /**
     * Returns the time represented by the snapshot as "phrase.bar.beat".
     *
     * @return a concise textual representation of the current metronome position at the time of the snapshot
     */
    public String getMarker () {
        return phrase + "." + getBarWithinPhrase() + "." + getBeatWithinBar();
    }
}
