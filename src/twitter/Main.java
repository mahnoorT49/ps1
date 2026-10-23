/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.net.URI;

/**
 * This is the main program.
 * 
 * You may change this class if you wish, but you don't have to.
 */
public class Main {
    
    /**
     * URL of a server that produces a list of tweets sampled from Twitter
     * within the last hour. This server may take up to a minute to respond, if
     * it has to refresh its cached sample of tweets.
     */
    public static final URL SAMPLE_SERVER = makeURLAssertWellFormatted("http://courses.csail.mit.edu/6.005/ps1_tweets/tweetPoll.py");
    
    private static URL makeURLAssertWellFormatted(String urlString) {
        try {
            return new URI(urlString).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Main method of the program. Fetches a sample of tweets and prints some
     * facts about it.
     * 
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            assert false;
            throw new Error("Always run main and tests with assertions enabled");
        } catch (AssertionError ae) { }
        
        final List<Tweet> tweets;
        try {
        	tweets = TweetReader.readTweetsFromFile("tweets.txt");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        
     // display some characteristics about the tweets
        final Timespan span = Extract.getTimespan(tweets);
        System.out.println("read " + tweets.size() + " tweets spanning [" + span.getStart() + "..." + span.getEnd() + "]");

        final Set<String> mentionedUsers = Extract.getMentionedUsers(tweets);
        System.out.println("mentioned users: " + mentionedUsers);
        
        // print tweets written by alyssa
        System.out.println();
        System.out.println("tweets written by alyssa:");
        for (Tweet t : Filter.writtenBy(tweets, "alyssa")) {
            System.out.println("(" + t.getId() + "  " + t.getTimestamp() + " "
                    + t.getAuthor() + ") " + t.getText());
        }
        
        // print tweets containing "rivest" or "pset"
        System.out.println();
        System.out.println("tweets containing 'rivest' or 'pset':");
        for (Tweet t : Filter.containing(tweets, Arrays.asList("rivest", "pset"))) {
            System.out.println("(" + t.getId() + "  " + t.getTimestamp() + " "
                    + t.getAuthor() + ") " + t.getText());
        }
        
        // Infer the follow graph from the tweets.
        Map<String, Set<String>> followsGraph =
                SocialNetwork.guessFollowsGraph(tweets);

        // Calculate the number of followers for each user.
        Map<String, Integer> followerCounts = new HashMap<>();

        for (String user : followsGraph.keySet()) {
            followerCounts.putIfAbsent(user, 0);

            for (String followedUser : followsGraph.get(user)) {
                followerCounts.put(followedUser,followerCounts.getOrDefault(followedUser, 0) + 1);
            }
        }

        // Get users ranked by influence.
        List<String> rankedUsers = SocialNetwork.influencers(followsGraph);

        // Print the top 18 influencers.
        System.out.println();
        System.out.println("Top 18 influencers:");

        int limit = Math.min(18, rankedUsers.size());

        for (int i = 0; i < limit; i++) {
            String user = rankedUsers.get(i);
            int count = followerCounts.getOrDefault(user, 0);

            System.out.println(
                    user + " (" + count
                    + (count == 1 ? " follower)" : " followers)")
            );
        }
    }
    
}