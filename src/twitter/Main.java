/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

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
    }
    
}