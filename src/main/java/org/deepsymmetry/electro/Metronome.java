package org.deepsymmetry.electro;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>A beat management tool. Tell it what BPM you want, and it will compute beat, bar, and phrase timestamps
 * accordingly. If you only need a single piece of information, you can obtain it directly from the metronome.
 * If you need to work with two or more values, you need to call {@link #getSnapshot()} and ask the snapshot
 * for them or you will see inconsistent results, since time will move on between each question you ask the
 * metronome itself.</p>
 *
 * <p>Inspired by Jeff Rose's work in
 * <a href="https://github.com/overtone/overtone/blob/master/src/overtone/music/rhythm.clj"
 *    target="blank">Overtone</a>.</p>
 *
 * @author James Elliott
 */
@SuppressWarnings("WeakerAccess")
public class Metronome implements Snapshot {

    /**
     * The millisecond timestamp at which the beat grid originates.
     */
    private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

    /**
     * The number of beats per second.
     */
    private final AtomicReference<Double> tempo = new AtomicReference<Double>(120.0);

    /**
     * The number of beats in a bar.
     */
    private final AtomicInteger beatsPerBar = new AtomicInteger(4);

    /**
     * The number of bars in a phrase.
     */
    private final AtomicInteger barsPerPhrase = new AtomicInteger(8);

    /**
     * Create a new metronome with default configuration. Its start time is now, its tempo is 120.0 beats per minute,
     * counting four beats per bar, and eight bars per phrase.
     */
    public Metronome() {
    }

    /**
     * Create a metronome which is a copy of another metronome, that is sharing the same start time, tempo, beats
     * per bar, and  bars per phrase. Once created, the metronomes are independent, so changes to one will not
     * affect the other.
     *
     * @param template the metronome whose configuration is to be copied
     */
    public Metronome(Metronome template) {
        startTime.set(template.getStartTime());
        tempo.set(template.getTempo());
        beatsPerBar.set(template.getBeatsPerBar());
        barsPerPhrase.set(template.getBarsPerPhrase());
    }

    /**
     * Returns the time at which this metronome was effectively started (tempo changes will shift this, as will
     * a variety of methods which adjust the timeline).
     *
     * @return the millisecond timestamp at which the beat grid originates.
     */
    @Override
    public long getStartTime() {
        return startTime.get();
    }

    /**
     * Adds a number of milliseconds to the start time of the metronome. Useful to nudge it back into synchronization
     * with an external source.
     *
     * @param ms the number of milliseconds to add to the start time
     */
    public synchronized void adjustStart(long ms) {
        startTime.addAndGet(ms);
    }

    /**
     * Get the tempo at which this metronome is running.
     *
     * @return the number of beats per minute being counted
     */
    @Override
    public double getTempo() {
        return tempo.get();
    }

    /**
     * Establish a new tempo for the metronome. The start time will be adjusted so that the current beat and phase
     * are unaffected by the tempo change.
     *
     * @param bpm the number of beats per minute at which the metronome should now run
     */
    public synchronized void setTempo(double bpm) {
        final long instant = System.currentTimeMillis();
        final long start = startTime.get();
        final double interval = getBeatInterval();
        final long beat = markerNumber(instant, start, interval);
        final double phase = markerPhase(instant, start, interval);
        final double newInterval = beatsToMilliseconds(1, bpm);
        startTime.set(instant - Math.round((newInterval * (phase + beat - 1))));
        tempo.set(bpm);
    }

    /**
     * Get the number of beats per bar in the metronome's beat grid. The default value is four.
     *
     * @return the number of beats per bar being counted
     */
    @Override
    public int getBeatsPerBar() {
        return beatsPerBar.get();
    }

    /**
     * Establish a new number of beats per bar in the metronome's beat grid.
     *
     * @param beatsPerBar a positive number of beats per bar being counted
     *
     * @throws IllegalArgumentException if {@code beatsPerBar} is not greater than zero
     */
    public void setBeatsPerBar(int beatsPerBar) {
        if (beatsPerBar > 0) {
            this.beatsPerBar.set(beatsPerBar);
        } else {
            throw new IllegalArgumentException("beatsPerBar must be greater than zero");
        }
    }

    /**
     * Get the number of bars per phrase in the metronome's beat grid.
     *
     * @return the number of bars per phrase being counted
     */
    @Override
    public int getBarsPerPhrase() {
        return barsPerPhrase.get();
    }

    /**
     * Establish a new number of bars per phrase in the metronome's beat grid.
     *
     * @param barsPerPhrase a positive number of bars per phrase being counted
     *
     * @throws IllegalArgumentException if {@code barsPerPhrase} is not greater than zero
     */
    public void setBarsPerPhrase(int barsPerPhrase) {
        if (barsPerPhrase > 0) {
            this.barsPerPhrase.set(barsPerPhrase);
        } else {
            throw new IllegalArgumentException("barsPerPhrase must be greater than zero");
        }
    }

    /**
     * Calculate the number of milliseconds taken by the specified number of beats at the specified tempo.
     *
     * @param beats the number of beats to time
     * @param tempo the number of that play in a minute
     *
     * @return the number of milliseconds that would pass while that many beats play at that tempo
     */
    public static double beatsToMilliseconds(long beats, double tempo) {
        return (60000.0 / tempo) * beats;
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
    public static long markerNumber(long instant, long start, double interval) {
        return (long)Math.floor((instant - start) / interval) + 1;
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
    public static double markerPhase(long instant, long start, double interval) {
        final double ratio = (instant - start) / interval;
        return ratio - Math.floor(ratio);
    }

    /**
     * <p>Helper function to calculate phase with respect to multiples or fractions of a marker
     * (beat, bar, or phrase), given the phase with respect to that marker, the marker number,
     * and the desired ratio. A {@code desiredRatio} of {@code 1.0} returns the phase unchanged;
     * {@code 0.5} (1/2) oscillates twice as fast, {@code 0.75} (3/4) oscillates 4 times every 3 markers...</p>
     *
     * <p>See the <a href="https://afterglow-guide.deepsymmetry.org/afterglow/oscillators.html#ratios" target="blank">
     *     Ratios illustration</a> in the Afterglow documentation for more details with graphs.</p>
     *
     * <p><em>Only positive values were considered for the ratio when writing this algorithm, the results you'll
     * get if you pass in zero or a negative value, are not likely meaningful.</em></p>
     *
     * @param markerNumber the current marker number being considered, as returned by {@link #markerNumber(long, long, double)}
     * @param markerPhase the current phase with respect to the marker, as returned by {@link #markerPhase(long, long, double)}
     * @param desiredRatio the ratio by which to oscillate the phase
     *
     * @return the oscillated phase
     *
     * @see #enhancedPhase(long, double, long, long)
     * @since 0.1.1
     */
    public static double enhancedPhase(long markerNumber, double markerPhase, double desiredRatio) {
        final BigFraction fraction = new BigFraction(desiredRatio, 0.00000002D, 10000);
        return enhancedPhase(markerNumber, markerPhase, fraction.getNumeratorAsLong(), fraction.getDenominatorAsLong());
    }

    /**
     * <p>Helper function to calculate phase with respect to multiples or fractions of a marker
     * (beat, bar, or phrase), given the phase with respect to that marker, the marker number,
     * and the desired ratio. A ration of 1/1 returns the phase unchanged; 1/2 oscillates
     * twice as fast, 3/4 oscillates 4 times every 3 markers...</p>
     *
     * <p>See the <a href="https://afterglow-guide.deepsymmetry.org/afterglow/oscillators.html#ratios" target="blank">
     *     Ratios illustration</a> in the Afterglow documentation for more details with graphs.</p>
     *
     * <p><em>Only positive values were considered for the numerator and denominator when writing this algorithm,
     * the results you'll get if you pass in zero or a negative value, are not likely meaningful.</em></p>
     *
     * @param markerNumber the current marker number being considered, as returned by {@link #markerNumber(long, long, double)}
     * @param markerPhase the current phase with respect to the marker, as returned by {@link #markerPhase(long, long, double)}
     * @param numerator over how many markers should an oscillation cycle span
     * @param denominator how many oscillations should occur in that span
     *
     * @return the oscillated phase
     *
     * @see #enhancedPhase(long, double, double)
     * @since 0.1.1
     */
    public static double enhancedPhase(long markerNumber, double markerPhase, long numerator, long denominator) {
        double basePhase = (numerator > 1) ?
                (((markerNumber - 1) % numerator) + markerPhase / numerator) :
                markerPhase;
        double adjustedPhase = basePhase * denominator;
        return adjustedPhase - Math.floor(adjustedPhase);
    }

    /**
     * Ensure that a phase falls in the range [0.0, 1.0). Values outside the range will have their non-fractional
     * part discarded.
     *
     * @param phase a phase value that may require normalization to within the unit range
     *
     * @return the normalized phase
     */
    public static double normalizePhase(double phase) {
        if (phase < 0.0) {
            return (phase - (long)phase) + 1.0;
        }
        return phase - (long)phase;
    }

    /**
     * Get the number of milliseconds a beat lasts given the current tempo.
     *
     * @return the duration of a beat
     */
    @Override
    public synchronized double getBeatInterval() {
        return beatsToMilliseconds(1, tempo.get());
    }

    /**
     * Get the number of milliseconds a bar lasts given the current configuration and tempo.
     *
     * @return the duration of a bar
     */
    @Override
    public synchronized double getBarInterval() {
        return beatsToMilliseconds(beatsPerBar.get(), tempo.get());
    }

    /**
     * Get the number of milliseconds a phrase lasts given the current configuration and tempo.
     *
     * @return the duration of a phrase
     */
    @Override
    public synchronized double getPhraseInterval() {
        return beatsToMilliseconds(beatsPerBar.get() * barsPerPhrase.get(), tempo.get());
    }

    /**
     * Get the current beat being played.
     *
     * @return the current beat number, which starts at 1
     */
    @Override
    public synchronized long getBeat() {
        return markerNumber(System.currentTimeMillis(), startTime.get(), getBeatInterval());
    }

    /**
     * Restarts the metronome at the beginning of the specified beat number.
     *
     * @param beat the beat to which the metronome should jump; the first beat is beat 1
     */
    public synchronized void jumpToBeat(long beat) {
        startTime.set(System.currentTimeMillis() - Math.round(((beat - 1) * getBeatInterval())));
    }

    /**
     * Determine the millisecond timestamp at which a particular beat will occur.
     *
     * @param beat the number of the beat whose start time is desired
     *
     * @return the time at which the specified beat begins, to the nearest millisecond
     */
    @Override
    public synchronized long getTimeOfBeat(long beat) {
        return Math.round((beat - 1) * getBeatInterval()) + startTime.get();
    }

    /**
     * Determine the distance traveled into the current beat as a phase number in the range [0.0, 1.0).
     *
     * @return the current beat phase
     */
    @Override
    public synchronized double getBeatPhase() {
        return markerPhase(System.currentTimeMillis(), startTime.get(), getBeatInterval());
    }

    /**
     * Figures out the least disruptive phase shift that ends up in a target phase.
     *
     * @param delta the amount to be added to our current phase to achieve a desired phase
     *
     * @return an amount that will yield the same phase while changing our position the least
     */
    public static double findClosestDelta(double delta) {
        return (delta > 0.5 ? (delta - 1.0) : (delta < -0.5 ? (delta + 1.0) : delta));
    }

    /**
     * Nudge the metronome so that it has reached the specified part of its current beat. If the value supplied is
     * outside the range of a beat phase (less than zero or greater than or equal to one), it will be normalized to
     * fit into that range by ignoring the non-fractional part.
     *
     * @param phase the desired beat phase, in the range [0.0, 1.0).
     */
    public synchronized void setBeatPhase(double phase) {
        final double delta = findClosestDelta(normalizePhase(phase) - getBeatPhase());
        final long shift = Math.round(getBeatInterval() * delta);
        startTime.addAndGet(-shift);
    }

    /**
     * Get the current bar being played.
     *
     * @return the current bar number, which starts at 1
     */
    @Override
    public synchronized long getBar() {
        return markerNumber(System.currentTimeMillis(), startTime.get(), getBarInterval());
    }

    /**
     * Restarts the metronome at the start of the specified bar, keeping the beat phase unchanged in case
     * it is being synchronized to an external source.
     *
     * @param bar the bar to which the metronome should jump; the first bar is bar 1
     */
    public synchronized void jumpToBar(long bar) {
        final double phase = getBeatPhase();
        final double closestPhase = (phase > 0.5)? (phase - 1.0) : phase;
        final double shift = getBeatInterval() * closestPhase;
        startTime.set(System.currentTimeMillis() - Math.round(shift - ((bar - 1) * getBarInterval())));
    }

    /**
     * Determine the millisecond timestamp at which a particular bar will occur.
     *
     * @param bar the number of the bar whose start time is desired
     *
     * @return the time at which the specified bar begins, rounded to the nearest millisecond
     */
    @Override
    public synchronized long getTimeOfBar(long bar) {
        return Math.round((bar - 1) * getBarInterval()) + startTime.get();
    }

    /**
     * Determine the distance traveled into the current bar as a phase number in the range [0.0, 1.0).
     *
     * @return the current bar phase
     */
    @Override
    public synchronized double getBarPhase() {
        return markerPhase(System.currentTimeMillis(), startTime.get(), getBarInterval());
    }

    /**
     * Nudge the metronome so that it has reached the specified part of its current bar. If the value supplied is
     * outside the range of a bar phase (less than zero or greater than or equal to one), it will be normalized to
     * fit into that range by ignoring the non-fractional part.
     *
     * @param phase the desired bar phase, in the range [0.0, 1.0).
     */
    public synchronized void setBarPhase(double phase) {
        final double delta = findClosestDelta(normalizePhase(phase) - getBarPhase());
        final long shift = Math.round(getBarInterval() * delta);
        startTime.addAndGet(-shift);
    }

    /**
     * Get the current phrase being played.
     *
     * @return the current phrase number, which starts at 1
     */
    @Override
    public synchronized long getPhrase() {
        return markerNumber(System.currentTimeMillis(), startTime.get(), getPhraseInterval());
    }

    /**
     * Restarts the metronome at the start of the specified phrase, keeping the beat phase unchanged in case
     * it is being synchronized to an external source.
     *
     * @param phrase the phrase to which the metronome should jump; the first phrase is phrase 1
     */
    public synchronized void jumpToPhrase(long phrase) {
        final double phase = getBeatPhase();
        final double closestPhase = (phase > 0.5)? (phase - 1.0) : phase;
        final double shift = getBeatInterval() * closestPhase;
        startTime.set(System.currentTimeMillis() - Math.round(shift - ((phrase - 1) * getPhraseInterval())));
    }

    /**
     * Determine the millisecond timestamp at which a particular phrase will occur.
     *
     * @param phrase the number of the phrase whose start time is desired
     *
     * @return the time at which the specified phrase begins, rounded to the nearest millisecond
     */
    @Override
    public synchronized long getTimeOfPhrase(long phrase) {
        return Math.round((phrase - 1) * getPhraseInterval()) + startTime.get();
    }

    /**
     * Determine the distance traveled into the current phrase as a phase number in the range [0.0, 1.0).
     *
     * @return the current phrase phase
     */
    @Override
    public synchronized double getPhrasePhase() {
        return markerPhase(System.currentTimeMillis(), startTime.get(), getPhraseInterval());
    }

    /**
     * Nudge the metronome so that it has reached the specified part of its current phrase. If the value supplied is
     * outside the range of a phrase phase (less than zero or greater than or equal to one), it will be normalized to
     * fit into that range by ignoring the non-fractional part.
     *
     * @param phase the desired phrase phase, in the range [0.0, 1.0).
     */
    public synchronized void setPhrasePhase(double phase) {
        final double delta = findClosestDelta(normalizePhase(phase) - getPhrasePhase());
        final long shift = Math.round(getPhraseInterval() * delta);
        startTime.addAndGet(-shift);
    }

    /**
     * Take a snapshot of the current beat, bar, phrase, and phase state, so coherent calculations about them can be
     * performed with respect to a static point in time.
     *
     * @return a representation of the detailed metronome state at the current moment
     */
    public synchronized Snapshot getSnapshot() {
        return new MetronomeSnapshot(this);
    }

    /**
     * Take a snapshot of the beat, bar, phrase, and phase state that the metronome would have at the specified
     * millisecond timestamp, so coherent calculations about them can be performed with respect to that static point
     * in time.
     *
     * @param instant the point in time which this snapshot should capture
     *
     * @return a representation of the detailed metronome state at the specified moment
     */
    public synchronized Snapshot getSnapshot(long instant) {
        return new MetronomeSnapshot(this, instant);
    }

    /**
     * Returns the current count of the metronome as "phrase.bar.beat".
     *
     * @return a concise textual representation of the current metronome position
     */
    @Override
    public String getMarker () {
        return getSnapshot().getMarker();
    }

    /**
     * Checks when a snapshot was taken; since you are working with a live metronome, always returns the current
     * time. If you are doing computations around this, you probably want to call {@link #getSnapshot()} and work
     * with that instead.
     *
     * @return the current system time in milliseconds
     */
    @Override
    public long getInstant() {
        return System.currentTimeMillis();
    }

    /**
     * Return the current beat number relative to the start of the bar: the down beat is 1, and the range
     * goes up to the value of {@link #getBeatsPerBar()}.
     *
     * @return the beat number within the current bar being counted
     */
    @Override
    public int getBeatWithinBar() {
        final double beatSize = 1.0 / beatsPerBar.get();
        return 1 + (int)Math.floor(getBarPhase() / beatSize);
    }

    /**
     * Checks whether the current beat is the first beat in its bar.
     *
     * @return {@code true} we are currently in the first beat of a bar
     */
    @Override
    public boolean isDownBeat() {
        return getBeatWithinBar() == 1;
    }

    /**
     * Return the current beat number relative to the start of the phrase: the phrase starts with beat 1, and
     * the range goes up to the value of {@link #getBeatsPerBar()} times {@link #getBarsPerPhrase()}.
     *
     * @return the beat number within the current phrase being counted
     */
    @Override
    public int getBeatWithinPhrase() {
        final double beatSize = 1.0 / (beatsPerBar.get() * barsPerPhrase.get());
        return 1 + (int)Math.floor(getPhrasePhase() / beatSize);
    }

    /**
     * Checks whether the current beat is the first beat in its phrase.
     *
     * @return {@code true} if we are currently in the first beat of a phrase
     */
    @Override
    public boolean isPhraseStart() {
        return getBeatWithinPhrase() == 1;
    }

    /**
     * Return the current bar number relative to the start of the phrase: the phrase starts with bar 1, and the
     * range goes up to the value of {@link #getBarsPerPhrase()}.
     *
     * @return the bar number within the current phrase being counted
     */
    @Override
    public int getBarWithinPhrase() {
        final double barSize = 1.0 / barsPerPhrase.get();
        return 1 + (int)Math.floor(getPhrasePhase() / barSize);
    }

    /**
     * Determine how far in time the metronome is from the closest beat. The result will be positive if the beat has
     * already occurred, and negative if it is coming up.
     *
     * @return the distance in milliseconds from the closest beat on the metronome's timeline
     */
    @Override
    public double distanceFromBeat() {
        final double phaseDistance = Metronome.findClosestDelta(getBeatPhase());
        return phaseDistance * getBeatInterval();
    }

    /**
     * Determine how far in time the metronome is from its closest bar boundary. The result will be positive if
     * the bar has already started, and negative if it is coming up.
     *
     * @return the distance in milliseconds from the closest bar boundary on the metronome's timeline
     */
    @Override
    public double distanceFromBar() {
        final double phaseDistance = Metronome.findClosestDelta(getBarPhase());
        return phaseDistance * getBarInterval();
    }

    /**
     * Determine how far in time the metronome is from its closest phrase boundary. The result will be positive if
     * the phrase has already started, and negative if it is coming up.
     *
     * @return the distance in milliseconds from the closest phrase boundary on the metronome's timeline
     */
    @Override
    public double distanceFromPhrase() {
        final double phaseDistance = Metronome.findClosestDelta(getPhrasePhase());
        return phaseDistance * getPhraseInterval();
    }

    @Override
    public String toString() {
        return "Metronome[" + getSnapshot().toString() + "]";
    }
}
