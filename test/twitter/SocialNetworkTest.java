/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkTest {

	/*
	 * Testing strategy for guessFollowsGraph(tweets):
	 * - Number of tweets: 0, 1, more than 1
	 * - Mentions in a tweet: 0, 1, more than 1
	 * - Tweets by the same author: 1, more than 1
	 * - Case of mentioned username: same, different
	 *
	 * Testing strategy for influencers(followsGraph):
	 * - Users in graph: 0, 1, more than 1
	 * - Followers of the top user: 0, 1, more than 1
	 * - Follower counts: all different, tied
	 */

	private long nextTweetId = 1L;

	private Tweet tweet(String author, String text) {
	    return new Tweet(nextTweetId++, author, text, java.time.Instant.parse("2020-01-01T00:00:00Z"));
	}

	// 1. Empty list of tweets
	@Test
	public void testGuessFollowsGraphEmpty() {
	    Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(new ArrayList<Tweet>());

	    assertTrue("expected empty graph", graph.isEmpty());
	}

	// 2. Tweets without mentions
	@Test
	public void testTweetsWithoutMentions() {
	    List<Tweet> tweets = Arrays.asList(
	            tweet("alice", "Hello everyone"),
	            tweet("bob", "Nice weather today")
	    );

	    assertTrue(
	            SocialNetwork.guessFollowsGraph(tweets).isEmpty()
	    );
	}

	// 3. Single mention
	@Test
	public void testSingleMention() {
	    List<Tweet> tweets = Collections.singletonList(
	            tweet("ernie", "Hello @bert")
	    );

	    Map<String, Set<String>> graph =
	            SocialNetwork.guessFollowsGraph(tweets);

	    assertEquals(Collections.singleton("bert"), graph.get("ernie"));
	}

	// 4. Multiple mentions in one tweet
	@Test
	public void testMultipleMentions() {
	    List<Tweet> tweets = Collections.singletonList(
	            tweet("alice", "Hello @bob and @charlie")
	    );

	    Map<String, Set<String>> graph =
	            SocialNetwork.guessFollowsGraph(tweets);

	    assertEquals(
	            new HashSet<String>(Arrays.asList("bob", "charlie")),
	            graph.get("alice")
	    );
	}

	// 5. Multiple tweets from one user
	@Test
	public void testMultipleTweetsFromSameAuthor() {
	    List<Tweet> tweets = Arrays.asList(
	            tweet("alice", "Hello @bob"),
	            tweet("alice", "Hello @charlie"),
	            tweet("alice", "Again @bob")
	    );

	    Map<String, Set<String>> graph =
	            SocialNetwork.guessFollowsGraph(tweets);

	    assertEquals(
	            new HashSet<String>(Arrays.asList("bob", "charlie")),
	            graph.get("alice")
	    );
	}

	// Check that username case differences are handled
	@Test
	public void testMentionUsernameDifferentCase() {
	    List<Tweet> tweets = Arrays.asList(
	            tweet("alice", "Hello @BOB"),
	            tweet("alice", "Hello @bob")
	    );

	    Map<String, Set<String>> graph =
	            SocialNetwork.guessFollowsGraph(tweets);

	    assertEquals(Collections.singleton("bob"), graph.get("alice"));
	}

	// Check tweets from multiple authors
	@Test
	public void testMultipleAuthors() {
	    List<Tweet> tweets = Arrays.asList(
	            tweet("alice", "Hello @bob and @charlie"),
	            tweet("david", "Hello @alice")
	    );

	    Map<String, Set<String>> graph =
	            SocialNetwork.guessFollowsGraph(tweets);

	    assertEquals(
	            new HashSet<String>(Arrays.asList("bob", "charlie")),
	            graph.get("alice")
	    );
	    assertEquals(Collections.singleton("alice"), graph.get("david"));
	}

	// 6. Empty graph for influencers()
	@Test
	public void testInfluencersEmpty() {
	    Map<String, Set<String>> graph = new HashMap<String, Set<String>>();

	    List<String> result = SocialNetwork.influencers(graph);

	    assertTrue("expected empty list", result.isEmpty());
	}

	// 7. Single user without followers
	@Test
	public void testSingleUserWithoutFollowers() {
	    Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
	    graph.put("alice", Collections.<String>emptySet());

	    List<String> result = SocialNetwork.influencers(graph);

	    assertEquals(Collections.singletonList("alice"), result);
	}

	// 8. Single influencer
	@Test
	public void testSingleInfluencer() {
	    Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
	    graph.put("alice", Collections.singleton("bob"));
	    graph.put("charlie", Collections.singleton("bob"));

	    List<String> result = SocialNetwork.influencers(graph);

	    assertEquals("bob", result.get(0));
	}

	// 9. Multiple influencers with different follower counts
	@Test
	public void testMultipleInfluencersDifferentCounts() {
	    Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
	    graph.put("alice", new HashSet<String>(
	            Arrays.asList("bob", "charlie")));
	    graph.put("david", Collections.singleton("bob"));
	    graph.put("eve", Collections.singleton("bob"));

	    List<String> result = SocialNetwork.influencers(graph);

	    assertEquals(5, result.size());
	    assertEquals("bob", result.get(0));
	    assertEquals("charlie", result.get(1));
	}

	// 10. Tied influence
	@Test
	public void testTiedInfluence() {
	    Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
	    graph.put("alice", new HashSet<String>(
	            Arrays.asList("bob", "david")));
	    graph.put("charlie", new HashSet<String>(
	            Arrays.asList("bob", "david")));

	    List<String> result = SocialNetwork.influencers(graph);

	    assertEquals(4, result.size());

	    // Bob and David each have two followers.
	    assertTrue(result.indexOf("bob") < 2);
	    assertTrue(result.indexOf("david") < 2);

	    // Alice and Charlie each have zero followers.
	    assertTrue(result.indexOf("alice") >= 2);
	    assertTrue(result.indexOf("charlie") >= 2);
	}
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * SocialNetwork class that follows the spec. It will be run against several
     * staff implementations of SocialNetwork, which will be done by overwriting
     * (temporarily) your version of SocialNetwork with the staff's version.
     * DO NOT strengthen the spec of SocialNetwork or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in SocialNetwork, because that means you're testing a
     * stronger spec than SocialNetwork says. If you need such helper methods,
     * define them in a different class. If you only need them in this test
     * class, then keep them in this test class.
     */
    


}
