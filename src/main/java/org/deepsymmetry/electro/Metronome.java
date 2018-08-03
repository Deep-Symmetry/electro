package org.deepsymmetry.electro;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A beat management tool. Tell it what BPM you want, and it will compute beat, bar, and phrase timestamps
 * accordingly.
 *
 * Inspired by Jeff Rose's work in
 * <a href="https://github.com/overtone/overtone/blob/master/src/overtone/music/rhythm.clj">Overtone</a>.
 *
 * @author James Elliott
 */
public class Metronome {

    /**
     * The millisecond timestamp at which the beat grid originates.
     */
    private AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

    /**
     * The number of beats per second.
     */
    private AtomicReference<Double> tempo = new AtomicReference<Double>(120.0);

    /**
     * The number of beats in a bar.
     */
    private AtomicInteger beatsPerBar = new AtomicInteger(4);

    /**
     * The number of bars in a phrase.
     */
    private AtomicInteger barsPerPhrase = new AtomicInteger(8);

    /**
     * Returns the time at which this metronome was effectively started (tempo changes will shift this, as will
     * a variety of methods which adjust the timeline).
     *
     * @return the millisecond timestamp at which the beat grid originates.
     */
    public long getStartTime() {
        return startTime.get();
    }

    /**
     * Calculate the number of milliseconds taken by the specified number of beats at the specified tempo.
     *
     * @param beats the number of beats to time
     * @param tempo the number of that play in a minute
     *
     * @return the number of milliseconds that would pass while that many beats play at that tempo
     */
    public long beatsToMilliseconds(long beats, double tempo) {
        return Math.round((60000.0 / tempo) * beats);
    }

    /**
     * Helper function to calculate the beat, bar, or phrase number in effect at a given instant (in milliseconds)
     * given a timeline starting point (start, also in milliseconds), and the interval (also in milliseconds) between
     * beats, bars, or phrases.
     *
     * @param instant the time (in milliseconds) for which a marker number is desired
     * @param start the time (in milliseconds) at which the metronome started counting
     * @param interval the time (in milliseconds) between markers
     *
     * @return the marker number currently in effect for the metronome at the specified instant
     */
    public long markerNumber(long instant, long start, long interval) {
        return ((instant - start) / interval) + 1;
    }

    /**
     * Helper function to calculate the beat, bar, or phrase phase at a given instant (in milliseconds),
     * given a timeline starting point (start, also in milliseconds), and the interval (also in milliseconds) between
     * beats, bars, or phrases. A marker phase starts at 0.0 at the beginning of the beat, bar, or phrase, rises
     * linearly during the beat, bar, or phrase, but never reaches 1.0, because that is the start of the next
     * beat, bar, or phrase.
     *
     * @param instant the time (in milliseconds) for which a marker phase is desired
     * @param start the time (in milliseconds) at which the metronome started counting
     * @param interval the time (in milliseconds) between markers
     *
     * @return the phase in effect for the specified marker at the specified instant, in the range [0.0, 1.0)
     */
    public double markerPhase(long instant, long start, long interval) {
        final double ratio = (instant - start) / (double)interval;
        return ratio - Math.floor(ratio);
    }

    /**
     * Ensure that a phase falls in the range [0.0, 1.0).
     *
     * @return the normalized phase.
     */
    public double normalizePhase(double phase) {
        if (phase < 0.0) {
            return (phase - (long)phase) + 1.0;
        }
        return phase - (long)phase;
    }


}
