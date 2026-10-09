/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Locale;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

	/*
	 * Testing strategy
	 *
	 * Partition the inputs as follows:
	 * tweets.size(): 1, > 1
	 * timestamp order: sorted, unsorted
	 * timestamp values: same, different
	 *
	 * Include unsorted timestamps to check that the minimum
	 * and maximum are found regardless of list order.
	 *
	 * Testing strategy for getMentionedUsers():
	 * tweets.size(): 1, > 1
	 * mentions per tweet: 0, 1, > 1
	 * mention case: lowercase, uppercase, mixed
	 * mention position: beginning, middle, end
	 * username characters: letters, digits, _, -
	 * mention boundary: valid, preceded by username character
	 * email address: present, absent
	 *
	 * Normalize usernames to lowercase before comparison,
	 * since capitalization of returned usernames is unspecified.
	*/
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T13:15:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
	private static final Tweet tweet3 = new Tweet(3, "sara", "Hello @Nadia, please contact @hamza_2", d3);
	private static final Tweet tweet4 = new Tweet(4, "nadia", "Thanks @NADIA! Send the file to test@mit.edu", d2);
	private static final Tweet tweet5 = new Tweet(5, "hamza", "Meet me later, @hamza-2", d1);

	private static Set<String> normalizeUsernames(Set<String> usernames) {
	    Set<String> normalized = new HashSet<>();
	
	    for (String username : usernames) {
	        normalized.add(username.toLowerCase(Locale.ROOT));
	    }
	
	    return normalized;
	}

    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }
    
    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1));
        
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }


    // Covers: a list containing only one tweet.
    @Test
    public void testSingleTweetTimeRange() {
        Timespan result = Extract.getTimespan(Arrays.asList(tweet1));

        assertEquals(d1, result.getStart());
        assertEquals(d1, result.getEnd());
    }

    // Covers: timestamps in ascending order.
    @Test
    public void testTimeRangeInOrder() {
        Timespan result = Extract.getTimespan(Arrays.asList(tweet1, tweet2));

        assertEquals(d1, result.getStart());
        assertEquals(d2, result.getEnd());
    }

    // Covers: the earliest and latest timestamps when the list is unsorted.
    @Test
    public void testTimeRangeUnsorted() {
        Timespan result =
                Extract.getTimespan(Arrays.asList(tweet3, tweet1, tweet2));

        assertEquals(d1, result.getStart());
        assertEquals(d3, result.getEnd());
    }

    // Covers: different tweets posted at the same instant.
    @Test
    public void testTimeRangeEqualTimestamps() {
        Tweet anotherTweet =
                new Tweet(6, "bilal", "Same time", d1);

        Timespan result =
                Extract.getTimespan(Arrays.asList(tweet1, anotherTweet));

        assertEquals(d1, result.getStart());
        assertEquals(d1, result.getEnd());
    }

    // Covers: text without any username mentions.
    @Test
    public void testNoUsernamesFound() {
        Set<String> result =
                Extract.getMentionedUsers(Arrays.asList(tweet1));

        assertTrue(result.isEmpty());
    }

    // Covers: a mention at the beginning of the message.
    @Test
    public void testMentionAtBeginning() {
        Tweet tweet = new Tweet(7, "bilal", "@sara welcome!", d1);

        Set<String> result =
                normalizeUsernames(Extract.getMentionedUsers(Arrays.asList(tweet)));

        assertEquals(new HashSet<>(Arrays.asList("sara")), result);
    }

    // Covers: multiple distinct usernames in one message.
    @Test
    public void testSeveralUsersInOneTweet() {
        Set<String> result =
                normalizeUsernames(Extract.getMentionedUsers(Arrays.asList(tweet3)));

        assertEquals(new HashSet<>(Arrays.asList("nadia", "hamza_2")), result);
    }

    // Covers: duplicate mentions with different capitalization.
    @Test
    public void testUsernameDuplicatesIgnoreCase() {
        Tweet tweet =
                new Tweet(8, "bilal", "@Sara and @SARA and @sara", d1);

        Set<String> result =
                normalizeUsernames(Extract.getMentionedUsers(Arrays.asList(tweet)));

        assertEquals(new HashSet<>(Arrays.asList("sara")), result);
    }

    // Covers: collecting mentions from more than one tweet.
    @Test
    public void testMentionsAcrossTweets() {
        Set<String> result =
                normalizeUsernames(
                        Extract.getMentionedUsers(Arrays.asList(tweet3, tweet4)));

        assertEquals(new HashSet<>(Arrays.asList("nadia", "hamza_2")), result);
    }

    // Covers: an email address should not create a username mention.
    @Test
    public void testEmailAddressIsNotMention() {
        Set<String> result =
                normalizeUsernames(Extract.getMentionedUsers(Arrays.asList(tweet4)));

        assertEquals(new HashSet<>(Arrays.asList("nadia")), result);
        assertFalse(result.contains("mit"));
    }

    // Covers: usernames with hyphens and underscores.
    @Test
    public void testSpecialUsernameCharacters() {
        Set<String> result =
                normalizeUsernames(Extract.getMentionedUsers(Arrays.asList(tweet5)));

        assertEquals(new HashSet<>(Arrays.asList("hamza-2")), result);
    }

    // Covers: a mention followed by punctuation at the end of the text.
    @Test
    public void testMentionAtEndOfMessage() {
        Tweet tweet = new Tweet(9, "bilal", "See you, @sara!", d1);

        Set<String> result =
                normalizeUsernames(Extract.getMentionedUsers(Arrays.asList(tweet)));

        assertEquals(new HashSet<>(Arrays.asList("sara")), result);
    }

    // Covers: an at-sign preceded by a username character.
    @Test
    public void testAtSignInsideEmailDoesNotMatch() {
        Tweet tweet =
                new Tweet(10, "bilal", "Contact person@company for details", d1);

        Set<String> result =
                Extract.getMentionedUsers(Arrays.asList(tweet));

        assertTrue(result.isEmpty());
    }


}
