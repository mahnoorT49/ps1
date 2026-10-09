/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterTest {

	/*
	 * Testing strategy
	 *
	 * Partition the inputs as follows:
	 *
	 * writtenBy(tweets, username):
	 * tweets.size(): 0, 1, > 1
	 * matching tweets: 0, 1, > 1
	 * username case: same, different
	 *
	 * inTimespan(tweets, timespan):
	 * tweets.size(): 0, > 1
	 * matching tweets: none, some, all
	 * endpoints: included, excluded
	 *
	 * containing(tweets, words):
	 * words.size(): 0, 1, > 1
	 * matching tweets: none, some, all
	 * word case: same, different
	 * whole-word match: yes, partial only
	 *
	 * Check that results preserve input order.
	*/
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "Alyssa", "Talking about the pset tonight", d3);
    
    // Verify that assertions are enabled with VM argument -ea
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false;
    }

    // Multiple tweets, exactly one matches; result is singleton list
    @Test
    public void testWrittenByMultipleTweetsSingleResult() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "alyssa");
        
        assertEquals("expected singleton list", 1, writtenBy.size());
        assertTrue("expected list to contain tweet", writtenBy.contains(tweet1));
    }

    // Username case differs from tweet authors; matching is case-insensitive
    @Test
    public void testWrittenByDifferentCase() {
        List<Tweet> result =
                Filter.writtenBy(
                        Arrays.asList(tweet1, tweet2, tweet3), "ALYSSA");

        assertEquals(Arrays.asList(tweet1, tweet3), result);
    }

    // No tweets match the given username; result is empty
    @Test
    public void testWrittenByNoResults() {
        List<Tweet> result =
                Filter.writtenBy(Arrays.asList(tweet1, tweet2), "unknown");

        assertTrue(result.isEmpty());
    }

    // Empty input list yields empty result
    @Test
    public void testWrittenByEmptyList() {
        List<Tweet> result =
                Filter.writtenBy(Arrays.<Tweet>asList(), "alyssa");

        assertTrue(result.isEmpty());
    }

    // Single tweet that matches; result contains just that tweet
    @Test
    public void testWrittenBySingleTweetInput() {
        List<Tweet> result =
                Filter.writtenBy(Arrays.asList(tweet1), "alyssa");

        assertEquals(Arrays.asList(tweet1), result);
    }

    // Multiple matches; result preserves the order of the input list
    @Test
    public void testWrittenByPreservesInputOrder() {
        List<Tweet> result =
                Filter.writtenBy(Arrays.asList(tweet3, tweet1), "alyssa");

        assertEquals(Arrays.asList(tweet3, tweet1), result);
    }

    // Timespan covers all tweets; all are returned in input order
    @Test
    public void testInTimespanMultipleTweetsMultipleResults() {
        List<Tweet> result =
                Filter.inTimespan(
                        Arrays.asList(tweet1, tweet2),
                        new Timespan(d1, d3));

        assertEquals(Arrays.asList(tweet1, tweet2), result);
    }

    // Tweets exactly on the start and end instants are included
    @Test
    public void testInTimespanEndpointsIncluded() {
        List<Tweet> result =
                Filter.inTimespan(
                        Arrays.asList(tweet1, tweet2, tweet3),
                        new Timespan(d2, d3));

        assertEquals(Arrays.asList(tweet2, tweet3), result);
    }

    // Timespan boundaries fall between tweet timestamps; only middle tweet matches
    @Test
    public void testInTimespanEndpointsExcluded() {
        Instant start = Instant.parse("2016-02-17T10:30:00Z");
        Instant end = Instant.parse("2016-02-17T11:30:00Z");

        List<Tweet> result =
                Filter.inTimespan(
                        Arrays.asList(tweet1, tweet2, tweet3),
                        new Timespan(start, end));

        assertEquals(Arrays.asList(tweet2), result);
    }

    // Timespan is entirely after all tweets; result is empty
    @Test
    public void testInTimespanNoResults() {
        Instant start = Instant.parse("2016-02-18T10:00:00Z");
        Instant end = Instant.parse("2016-02-18T11:00:00Z");

        List<Tweet> result =
                Filter.inTimespan(
                        Arrays.asList(tweet1, tweet2),
                        new Timespan(start, end));

        assertTrue(result.isEmpty());
    }

    // Empty input list yields empty result
    @Test
    public void testInTimespanEmptyList() {
        List<Tweet> result =
                Filter.inTimespan(
                        Arrays.<Tweet>asList(), new Timespan(d1, d3));

        assertTrue(result.isEmpty());
    }

    // Single tweet within timespan; result contains just that tweet
    @Test
    public void testInTimespanSingleTweet() {
        List<Tweet> result =
                Filter.inTimespan(
                        Arrays.asList(tweet1), new Timespan(d1, d2));

        assertEquals(Arrays.asList(tweet1), result);
    }

    // Single word matches multiple tweets; result preserves input order
    @Test
    public void testContaining() {
        List<Tweet> result =
                Filter.containing(
                        Arrays.asList(tweet1, tweet2), Arrays.asList("talk"));

        assertEquals(Arrays.asList(tweet1, tweet2), result);
    }

    // Word matching is case-insensitive and whole-word only
    @Test
    public void testContainingDifferentCaseAndWholeWords() {
        List<Tweet> result =
                Filter.containing(
                        Arrays.asList(tweet1, tweet2, tweet3),
                        Arrays.asList("TALK"));

        assertEquals(Arrays.asList(tweet1, tweet2), result);
    }

    // Tweet matches if it contains any of the given words
    @Test
    public void testContainingMultipleWords() {
        List<Tweet> result =
                Filter.containing(
                        Arrays.asList(tweet1, tweet2, tweet3),
                        Arrays.asList("pset", "rivest"));

        assertEquals(Arrays.asList(tweet1, tweet2, tweet3), result);
    }

    // None of the words appear in any tweet; result is empty
    @Test
    public void testContainingNoMatches() {
        List<Tweet> result =
                Filter.containing(
                        Arrays.asList(tweet1, tweet2, tweet3),
                        Arrays.asList("computer"));

        assertTrue(result.isEmpty());
    }

    // Empty word list yields empty result
    @Test
    public void testContainingEmptyWords() {
        List<Tweet> result =
                Filter.containing(
                        Arrays.asList(tweet1, tweet2, tweet3),
                        Arrays.<String>asList());

        assertTrue(result.isEmpty());
    }

    // "talk" does not match "Talking" because matching is whole-word
    @Test
    public void testContainingDoesNotMatchPartialWords() {
        List<Tweet> result =
                Filter.containing(
                        Arrays.asList(tweet3), Arrays.asList("talk"));

        assertTrue(result.isEmpty());
    }

    // Multiple matches; result preserves the order of the input list
    @Test
    public void testContainingPreservesInputOrder() {
        List<Tweet> result =
                Filter.containing(
                        Arrays.asList(tweet2, tweet1), Arrays.asList("talk"));

        assertEquals(Arrays.asList(tweet2, tweet1), result);
    }

}
