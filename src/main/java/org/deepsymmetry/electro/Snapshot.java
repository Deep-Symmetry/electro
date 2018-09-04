package org.deepsymmetry.electro;

/**
 * <p>An interface for probing details about the timeline established by a metronome, with respect to a particular
 * moment in time. The {@link Metronome} class implements this interface and provides answers with respect to the
 * moment at which you called each function, but if you need to work with more than one piece of information at a
 * time, be sure to use {@link Metronome#getSnapshot()} so that each of your calculations refer to the same moment.
 * Otherwise you risk getting misleading results, such as when you ask for a beat number in one call, and then
 * the beat phase, and you have moved on to a different beat in between the calls.</p>
 *
 * <p>Snapshots in Afterglow also extend the notions of beat phase to enable oscillators with frequencies that are
 * fractions or multiples of a beat. As of version 0.1.1, this concept has been ported from the Clojure version to
 * here, but it is available as {@link Metronome#enhancedPhase(long, double, double)} rather than additional
 * overloads of the snapshot methods.</p>
 *
 * @author James Elliott
 */
@SuppressWarnings("WeakerAccess")
public interface Snapshot {

    /**
     * Get the metronome's timeline origin.
     *
     * @return the point in time at which the metronome from which this snapshot was taken started counting
     */
    long getStartTime();

    /**
     * Get the metronome's tempo.
     *
     * @return the number of beats per minute at which the metronome from which this snapshot was taken was running
     */
    double getTempo();

    /**
     * Get the metronome's bar length in beats.
     *
     * @return the number of beats which made up a bar in the metronome from which this snapshot was taken
     */
    int getBeatsPerBar();

    /**
     * Get the metronome's phrase length in bars.
     *
     * @return the number of beats which made up a bar in the metronome from which this snapshot was taken
     */
    int getBarsPerPhrase();

    /**
     * Get the point in time with respect to which the snapshot is computed. The difference between this and
     * {@link #getStartTime()}, along with {@link #getTempo()}, determine the other snapshot values.
     *
     * @return the moment represented by the snapshot
     */
    long getInstant();

    /**
     * Get the metronome's beat length in time.
     *
     * @return the duration of a beat, in milliseconds, at the tempo when the snapshot was taken
     */
    double getBeatInterval();

    /**
     * Get the metronome's bar length in time.
     *
     * @return the duration of a bar, in milliseconds, at the tempo when the snapshot was taken
     */
    double getBarInterval();

    /**
     * Get the metronome's phrase length in time.
     *
     * @return the duration of a phrase, in milliseconds, at the tempo when the snapshot was taken
     */
    double getPhraseInterval();

    /**
     * Get the metronome's beat number.
     *
     * @return the beat number being played at the time represented by the snapshot; beats start at 1
     */
    long getBeat();

    /**
     * Get the metronome's bar number.
     *
     * @return the bar number being played at the time represented by the snapshot; bars start at 1
     */
    long getBar();

    /**
     * Get the metronome's phrase number.
     *
     * @return the phrase number being played at the time represented by the snapshot; phrases start at 1
     */
    long getPhrase();

    /**
     * Get the metronome's beat phase at the time of the snapshot, a value which starts at 0.0 at the very start of the
     * beat, but never quite reaches 1.0, because that would be the start of the next beat.
     *
     * @return how far we have have traveled through the current beat at the point in time represented by the snapshot
     */
    double getBeatPhase();

    /**
     * Get the metronome's bar phase at the time of the snapshot, a value which starts at 0.0 at the very start of the
     * bar, but never quite reaches 1.0, because that would be the start of the next bar.
     *
     * @return how far we have traveled through the current bar at the point in time represented by the snapshot
     */
    double getBarPhase();

    /**
     * Get the metronome's phrase phase at the time of the snapshot, a value which starts at 0.0 at the very start of
     * the phrase, but never quite reaches 1.0, because that would be the start of the next phrase.
     *
     * @return how far we have traveled through the current phrase at the point in time represented by the snapshot
     */
    double getPhrasePhase();

    /**
     * Determine the millisecond timestamp at which a particular beat will occur, given the metronome configuration when
     * the snapshot was taken.
     *
     * @param beat the number of the beat whose start time is desired
     *
     * @return the time at which the specified beat begins, rounded to the nearest millisecond
     */
    long getTimeOfBeat(long beat);

    /**
     * Return the beat number of the snapshot relative to the start of the bar: the down beat is 1, and the range
     * goes up to the value of {@link #getBeatsPerBar()}.
     *
     * @return the beat number within the current bar being counted at the point in time represented by the snapshot
     */
    int getBeatWithinBar();

    /**
     * Checks whether the current beat at the time of the snapshot was the first beat in its bar.
     *
     * @return {@code true} if the snapshot was taken during the first beat of a bar
     */
    boolean isDownBeat();

    /**
     * Return the beat number of the snapshot relative to the start of the phrase: the phrase starts with beat 1, and
     * the range goes up to the value of {@link #getBeatsPerBar()} times {@link #getBarsPerPhrase()}.
     *
     * @return the beat number within the current phrase being counted at the point in time represented by the snapshot
     */
    int getBeatWithinPhrase();

    /**
     * Checks whether the current beat at the time of the snapshot was the first beat in its phrase.
     *
     * @return {@code true} if the snapshot was taken during the first beat of a phrase
     */
    boolean isPhraseStart();

    /**
     * Determine the millisecond timestamp at which a particular bar will occur, given the metronome configuration when
     * the snapshot was taken.
     *
     * @param bar the number of the bar whose start time is desired
     *
     * @return the time at which the specified bar begins, rounded to the nearest millisecond
     */
    long getTimeOfBar(long bar);

    /**
     * Return the bar number of the snapshot relative to the start of the phrase: the phrase starts with bar 1, and the
     * range goes up to the value of {@link #getBarsPerPhrase()}.
     *
     * @return the bar number within the current phrase being counted at the point in time represented by the snapshot
     */
    int getBarWithinPhrase();

    /**
     * Determine the millisecond timestamp at which a particular phrase will occur, given the metronome configuration
     * when the snapshot was taken.
     *
     * @param phrase the number of the phrase whose start time is desired
     *
     * @return the time at which the specified phrase begins, rounded to the nearest millisecond
     */
    long getTimeOfPhrase(long phrase);

    /**
     * Returns the time represented by the snapshot as "phrase.bar.beat".
     *
     * @return a concise textual representation of the current metronome position at the time of the snapshot
     */
    String getMarker ();
}
