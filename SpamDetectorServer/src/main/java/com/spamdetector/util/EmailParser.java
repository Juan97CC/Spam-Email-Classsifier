package com.spamdetector.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class EmailParser {

    /**
     * Reads all email files in the given directory and calculates the total word frequency across all emails.
     * @param emailDirectory the directory containing email files
     * @return a map of words to their total frequency in all emails
     */
    public Map<String, Integer> getWordFrequency(File emailDirectory){

        // Final map to store the cumulative word frequencies from all emails
        Map<String, Integer> wordFrequencyMap = new TreeMap<>();

        // Get all files in the directory
        File[] emailFiles = emailDirectory.listFiles();
        int emailsFilesCount = emailFiles.length;

        for (File email: emailFiles){
            // Generate word frequency map for the current email file
            Map<String, Integer> emailFrequencyMap = calculateWordFrequency(email);

            // Merge the current email's word frequencies into the final map
            Set<String> words = emailFrequencyMap.keySet();
            Iterator<String> iterator = words.iterator();

            while (iterator.hasNext()){
                String word = iterator.next();
                int wordCount = emailFrequencyMap.get(word);

                if (!wordFrequencyMap.containsKey(word)){
                    wordFrequencyMap.put(word, wordCount);
                } else {
                    int oldCount = wordFrequencyMap.get(word);
                    wordFrequencyMap.put(word, oldCount + wordCount);
                }
            }

        }

        return wordFrequencyMap;
    }

    /**
     * Calculates the word frequency for a single email file.
     * @param email the email file to analyze
     * @return a map of words to their frequency in the email
     */
    private Map<String, Integer> calculateWordFrequency(File email){
        Map<String, Integer> emailWordFrequencyMap = new TreeMap<>();

        try {
            Scanner emailScanner = new Scanner(email);

            while (emailScanner.hasNext()){
                String word = emailScanner.next().toLowerCase();

                if(isWord(word)){
                    // If the word is valid and not already counted, initialize its count
                    if (!emailWordFrequencyMap.containsKey(word)){
                        emailWordFrequencyMap.put(word, 1);
                    }
                    // If word is already counted, increment its frequency
                    else {
                        int count = emailWordFrequencyMap.get(word);
                        emailWordFrequencyMap.put(word, count + 1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Return the frequency map for the current email
        return emailWordFrequencyMap;
    }

    /**
     * Determines whether a given string qualifies as a valid word (only lowercase alphabetic characters).
     * @param word the string to validate
     * @return true if it matches the criteria for a valid word, false otherwise
     */
    public boolean isWord(String word){
        if(word == null || "".equals(word))
            return false;

        String acceptablePattern = "^[a-z]*$";
        return word.matches(acceptablePattern);
    }
}
