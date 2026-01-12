package com.spamdetector.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spamdetector.domain.TestFile;
import jakarta.ws.rs.core.Response;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * TODO: This class will be implemented by you.
 * This class is responsible for training a Naive Bayes spam classifier using word frequencies
 * and estimating the probability that a file is spam based on the words it contains.
 * You may create more methods to help you organize your strategy and make your code more readable.
 */
public class SpamDetector {
    // Jackson library object to serialize results to JSON
    ObjectMapper objectMapper = new ObjectMapper();

    // Count of training ham emails from two separate folders
    public int trainHamCount2 = totalFilesInFolder("train/ham");  // First ham training set
    public int trainHamCount = totalFilesInFolder("train/ham2");  // Second ham training set
    public int totalHamCount = trainHamCount + trainHamCount2;    // Total ham emails

    // Count of spam training emails
    public int trainSpamCount = totalFilesInFolder("train/spam");

    // Word frequency maps for ham emails
    public Map<String, Integer> trainHamFreq1 = frequMap("train/ham");   // From first ham folder
    public Map<String, Integer> trainHamFreq2 = frequMap("train/ham2");  // From second ham folder
    public Map<String, Integer> trainHamFreq = addMaps(trainHamFreq1, trainHamFreq2); // Combined ham frequency

    // Word frequency map for spam emails
    public Map<String, Integer> trainSpamFreq = frequMap("train/spam");

    // Word probability maps for Pr(Wi|S) and Pr(Wi|H)
    public Map<String, Double> probWordAppearsInSpam =
            calculateProb(trainSpamFreq, trainSpamCount); // P(word|spam)

    public Map<String, Double> probWordAppearsInHam =
            calculateProb(trainHamFreq, totalHamCount);   // P(word|ham)

    // The core probability map: for each word, what's the probability a file is spam given the word appears
    // This is Pr(S|Wi) = [P(Wi|S)] / [P(Wi|S) + P(Wi|H)]
    public Map<String, Double> probFileIsSpam = calcProbFileIsSpam(
            probWordAppearsInSpam, probWordAppearsInHam);

    // Final results for test files (probably for evaluation or output)
    public List<Map<String, Object>> result = finalResult("ham");

    /*
     * public List<TestFile> trainAndTest(File mainDirectory) {
     *     TODO: main method of loading the directories and files, training and testing the model;
     *     return new ArrayList<TestFile>();
     * }
     */

    /**
     * This method calculates the inverse probability Pr(S|Wi) for all words that appear
     * in either the spam or ham frequency map. It uses the pre-computed word probability maps.
     * @param spamMap P(Wi|S) - probability of word i given it's spam
     * @param hamMap  P(Wi|H) - probability of word i given it's ham
     * @return Map with key = word, value = Pr(S|Wi)
     */
    public Map<String, Double> calcProbFileIsSpam(Map<String, Double> spamMap, Map<String, Double> hamMap) {
        Map<String, Double> temp = new TreeMap<>();

        // First, add all unique words from spamMap and hamMap into 'temp' with initial value 0.0
        for (Map.Entry<String, Double> entry : spamMap.entrySet()) {
            String key = entry.getKey();
            temp.put(key, 0.0);
        }
        for (Map.Entry<String, Double> entry : hamMap.entrySet()) {
            String key = entry.getKey();
            temp.put(key, 0.0);
        }

        // Now calculate Pr(S|Wi) for each word and store it in 'temp'
        for (String word : temp.keySet()) {
            calculateAndStorePrWords(word, temp);
        }

        return temp;
    }

    /**
     * Method to calculate and store Pr(S|Wi) = P(Wi|S) / (P(Wi|S) + P(Wi|H))
     * for a given word. This is the key formula in Naive Bayes for spam classification.
     *
     * If the word does not appear in either spam or ham training data (both probabilities = 0),
     * then it defaults to 0.
     *
     * @param word The word whose spam probability we are calculating.
     * @param temp Map where the result will be stored.
     */
    public void calculateAndStorePrWords(String word, Map<String, Double> temp) {
        // Get probability of word appearing in spam and ham
        double prSpamWord = probWordAppearsInSpam.getOrDefault(word, 0.0);
        double prHamWord = probWordAppearsInHam.getOrDefault(word, 0.0);

        // If both are 0, no evidence for this word, set spam probability to 0
        if (prSpamWord + prHamWord == 0) {
            temp.put(word, 0.0);
        } else {
            // Apply the Naive Bayes spam probability formula
            temp.put(word, prSpamWord / (prSpamWord + prHamWord));
        }
    }





    //

    // Calculates probability that each word appears in the given map (spam or ham)

    /**
     * Calculates the probability of each word occurring in either the spam or ham training data.
     * The probability is calculated as count of the word divided by the total number of files.
     *
     * @param trainMap    Map of word frequencies (word -> count)
     * @param totalCount  Total number of files (emails) in the class
     * @return            Map of word -> probability
     */
    public Map<String, Double> calculateProb(Map<String, Integer> trainMap, int totalCount) {
        Map<String, Double> probabilityMap = new TreeMap<>();

        for (Map.Entry<String, Integer> entry : trainMap.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();

            // Calculate P(word | class) = word count / total email count
            double calcProbability = (double) count / totalCount;
            probabilityMap.put(word, calcProbability);
        }

        return probabilityMap;
    }


    /**
     * Gets the File object pointing to the directory for given relative location.
     *
     * @param loc  Relative path to the folder (e.g. "test/ham")
     * @return     File object representing that folder
     */
    public File getFileLocation(String loc) {
        URL url = this.getClass().getClassLoader().getResource("/data/" + loc);
        File emailDirectory = null;

        try {
            emailDirectory = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return emailDirectory;
    }


    /**
     * Returns the number of files (emails) in a given folder.
     *
     * @param folderPath  Relative folder path (e.g. "test/ham")
     * @return            Number of files in the folder
     */
    public int totalFilesInFolder(String folderPath) {
        File folder = getFileLocation(folderPath);
        File[] emailFiles = folder.listFiles();
        int count = emailFiles.length;
        return count;
    }


    /**
     * NOTE: Incomplete function - needs implementation.
     * Idea: Collect word frequency from test folder using EmailParser.
     *
     * @param loc  Relative path to test folder (e.g. "test/ham")
     * @return     Map of word -> frequency in test data
     */
    public Map<String, Integer> testMapFrequency(String loc) {
        File emailDirectory = getFileLocation(loc);
        EmailParser emailParser = new EmailParser();

        // TODO: Implement file loop and use parser to extract words
        return null; // Placeholder
    }


    /**
     * Constructs final result in the required output format:
     * Each entry looks like:
     * {"spamProbRounded":"0.00000", "file":"00006.654c4", "spamProbability":5.901957E-62, "actualClass":"Ham"}
     *
     * @param actualClass  Class label for actual data (e.g., "Ham" or "Spam")
     * @return             List of results, one per test file
     */
    public List<Map<String, Object>> finalResult(String actualClass) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            // Use test folder for given class (e.g., test/ham)
            File emailDirectory = getFileLocation("test/ham"); // FIXME: make dynamic
            File[] emailFiles = emailDirectory.listFiles();

            if (emailFiles != null) {
                for (File email : emailFiles) {
                    // Calculate probability that this email is spam
                    Map<String, Double> temp = calculateEmailProb(email);

                    for (Map.Entry<String, Double> entry : temp.entrySet()) {
                        String file = entry.getKey();
                        Double spamProb = entry.getValue();

                        // Format the result entry for this email
                        Map<String, Object> mapToBeInserted = new TreeMap<>();
                        mapToBeInserted.put("spamProbRounded", String.format("%.5f", spamProb));
                        mapToBeInserted.put("file", file);
                        mapToBeInserted.put("spamProbability", spamProb);
                        mapToBeInserted.put("actualClass", actualClass);

                        resultList.add(mapToBeInserted);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }



    //

    /**
     * This function uses the EmailParser class to tokenize emails in a folder,
     * and returns a map containing each word and the number of times it appears.
     *
     * @param loc  Relative path to the folder (e.g. "train/spam")
     * @return     Map of word -> frequency from all emails in the folder
     */
    public Map<String, Integer> frequMap(String loc) {
        File emailDirectory = getFileLocation(loc);         // Locate the email folder
        EmailParser emailParser = new EmailParser();        // Create parser to process the folder

        return emailParser.getWordFrequency(emailDirectory); // Parse and return word frequency map
    }


    /**
     * This function merges two word frequency maps into one.
     * If a word appears in both maps, their counts are added together.
     *
     * @param map1  First word frequency map
     * @param map2  Second word frequency map
     * @return      Combined map with total word counts
     */
    public Map<String, Integer> addMaps(Map<String, Integer> map1,
                                        Map<String, Integer> map2) {
        Map<String, Integer> resultMap = new TreeMap<>();

        // Add all entries from map1
        for (Map.Entry<String, Integer> entry : map1.entrySet()) {
            String key = entry.getKey();
            int value = entry.getValue();

            // If word exists already, add to it; otherwise, just insert
            resultMap.put(key, resultMap.getOrDefault(key, 0) + value);
        }

        // Add all entries from map2
        for (Map.Entry<String, Integer> entry : map2.entrySet()) {
            String key = entry.getKey();
            int value = entry.getValue();

            // Add to existing count if already present
            resultMap.put(key, resultMap.getOrDefault(key, 0) + value);
        }

        return resultMap;
    }


    /**
     * Calculates the probability that a given email file is spam using Naive Bayes.
     *
     * This function scans through all words in the file, checks the probability of
     * each word appearing in spam emails, and uses the Naive Bayes formula to estimate
     * the final probability score for the file being spam.
     *
     * @param email  The email file to analyze.
     * @return       A map with the email file name as the key and its spam probability as the value.
     */
    private Map<String, Double> calculateEmailProb(File email) {
        Double probabilityScore = 0.0;
        Double n = 0.0;

        EmailParser emailParser = new EmailParser();
        Map<String, Double> emailWordProbabilityMap = new TreeMap<>();

        try {
            Scanner emailScanner = new Scanner(email);

            while (emailScanner.hasNext()) {
                String word = emailScanner.next().toLowerCase();

                if (emailParser.isWord(word)) {
                    // Get the spam probability for this word (Pr(S|Wi))
                    double probIsSpam = probFileIsSpam.getOrDefault(word, 0.0);

                    // If the probability is valid (not 0 or 1), update the score
                    if (probIsSpam != 0 && probIsSpam != 1) {
                        // Naive Bayes log odds calculation: ln((1 - P) / P)
                        n += Math.log((1 - probIsSpam) / probIsSpam);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Email file not found: " + email.getName(), e);
        }

        // Final spam probability using sigmoid function: 1 / (1 + e^n)
        probabilityScore = 1 / (1 + Math.pow(Math.E, n));

        // Store the result with the email file name as key
        emailWordProbabilityMap.put(email.getName(), probabilityScore);

        return emailWordProbabilityMap;
    }

    //getTestWord


    /**
     * Retrieves the frequency of words in the given folder, calculates the probability
     * of the email being spam, and returns a map containing the filenames and their
     * corresponding spam probability scores.
     *
     * @param folder The folder path containing the email files to analyze.
     * @return A map with filenames as keys and their calculated spam probabilities as values.
     */
    public Map<String, Double> getTestWordFrequency(String folder) {

        // Get the directory containing email files
        File emailDirectory = getFileLocation(folder);

        /**
         * This map will store the file name as the key and the probability score
         * (spam probability) as the value.
         */
        Map<String, Double> emailFrequencyMap = new TreeMap<>();

        // Read all files in the directory
        File[] emailFiles = emailDirectory.listFiles();
        int emailFilesCount = emailFiles.length;

        // Iterate through each email file in the directory
        for (File email : emailFiles) {

            // Calculate the spam probability for the current email
            Map<String, Double> result = calculateEmailProb(email);

            // Add the calculated probability to the map
            emailFrequencyMap.putAll(result);
        }

        // Return the final map of filenames and their corresponding probability scores
        return emailFrequencyMap;
    }

    /**
     * This method constructs a response that contains the spam results as a JSON object.
     * The response will be formatted with necessary headers for CORS and content type.
     *
     * @return A Response object containing the result as a JSON entity.
     */
    public Response resultForGetSpam() {
        try {
            // Return the response with status 200, CORS headers, and the result as JSON
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "http://localhost:63342") // Allow CORS
                    .header("Content-Type", "application/json") // Set content type to JSON
                    .entity(objectMapper.writeValueAsString(result)) // Convert the result to JSON
                    .build();
        } catch (JsonProcessingException e) {
            // Handle the exception if JSON processing fails
            throw new RuntimeException("Error processing the result into JSON.", e);
        }
    }



    /**
     * This function calculates the total number of files present in the test folder.
     * It calls the `totalFilesInFolder` method for both the "test/ham" and "test/spam"
     * folders, and sums the number of files in each folder.
     *
     * @return The total count of files in the test/ham and test/spam folders.
     */
    public int getTotalFilesInTestFolder(){
        // Get the number of files in the "test/ham" folder
        int ham = totalFilesInFolder("test/ham");

        // Get the number of files in the "test/spam" folder
        int spam = totalFilesInFolder("test/spam");

        // Return the sum of both counts
        return (ham + spam);
    }

    /**
     * This function counts the number of correctly predicted emails based on their probability score.
     * It evaluates the emails in the specified folder (either "ham" or "spam"). If the folder type is "ham",
     * it increments the `totalCorrectCount` when the probability score of the email is less than 0.5 (indicating it is predicted as ham).
     * If the folder type is "spam", it increments the `totalCorrectCount` when the probability score of the email is greater than 0.5 (indicating it is predicted as spam).
     *
     * @param type The folder type to evaluate, either "ham" or "spam".
     * @return The count of correctly predicted emails.
     */
    public int countCorrectPredictedEmails(String type){
        // Initialize a variable to track the number of correct predictions
        int totalCorrectCount = 0;

        // If the type is "ham", we want to check if the probability is less than 0.5
        if (type.toLowerCase().compareTo("ham") == 0){
            // Get the frequency map for emails in the "test/ham" folder
            Map<String, Double> temp = getTestWordFrequency("test/ham");

            // Loop through each entry in the map
            for (Map.Entry<String, Double> entry: temp.entrySet()){
                // Get the probability value for this email
                Double value = entry.getValue();

                // If the probability is less than 0.5 (i.e., the email is classified as ham), increment the correct count
                if (value < 0.5)
                    totalCorrectCount += 1;
            }
            // Return the total number of correctly predicted ham emails
            return totalCorrectCount;
        }

        // If the type is not "ham", assume it's "spam" and check the "test/spam" folder
        Map<String, Double> temp = getTestWordFrequency("test/spam");

        // Loop through each entry in the spam map
        for (Map.Entry<String, Double> entry: temp.entrySet()){
            // Get the probability value for this email
            Double value = entry.getValue();

            // If the probability is greater than 0.5 (i.e., the email is classified as spam), increment the correct count
            if (value > 0.5)
                totalCorrectCount += 1;
        }

        // Return the total number of correctly predicted spam emails
        return totalCorrectCount;
    }

    /**
     * This function calculates the accuracy of the predictions made by the model.
     * Accuracy is calculated as the ratio of correct predictions (both ham and spam)
     * to the total number of files in the test set.
     *
     * @return The accuracy value as a Double.
     */
    public Double accuracyResult(){
        // Get the number of correctly predicted ham emails
        int correctPredictedHamEmails = countCorrectPredictedEmails("ham");

        // Get the number of correctly predicted spam emails
        int correctPredictedSpamEmails = countCorrectPredictedEmails("spam");

        // Get the total number of files in the test set (ham + spam)
        int totalFileCount = getTotalFilesInTestFolder();

        // Calculate accuracy as the sum of correct ham and spam predictions divided by the total number of files
        return (double)(correctPredictedHamEmails + correctPredictedSpamEmails) / totalFileCount;
    }

    /**
     * This function returns the accuracy value in a response format that can be sent back as JSON.
     *
     * @return A Response object containing the accuracy value as a JSON string.
     */
    public Response resultForAccuracy() {
        // Get the accuracy value
        Double accuracy = accuracyResult();

        try {
            // Return the accuracy value as a JSON response
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "http://localhost:63342")
                    .header("Content-Type", "application/json")
                    .entity(objectMapper.writeValueAsString("Accuracy value :" + accuracy))
                    .build();
        } catch (JsonProcessingException e) {
            // Handle any JSON processing errors
            throw new RuntimeException(e);
        }
    }

    /**
     * This function calculates the precision of the predictions made by the model.
     * Precision is calculated as the ratio of true positives (correctly predicted ham emails)
     * to the sum of true positives and false positives (incorrectly predicted ham emails).
     *
     * @return The precision value as a Double.
     */
    public Double precisionResult(){
        // Get the number of correctly predicted ham emails
        int correctPredictedHamEmails = countCorrectPredictedEmails("ham");

        // Get the total number of ham files in the test set
        int totalHamFiles = totalFilesInFolder("test/ham");

        // Calculate the number of incorrectly predicted ham emails
        int incorrectPredictedHamEmails = totalHamFiles - correctPredictedHamEmails;

        // Calculate precision as the ratio of correctly predicted ham emails to the sum of correct and incorrect predictions
        return (double)correctPredictedHamEmails / (incorrectPredictedHamEmails + correctPredictedHamEmails);
    }

    /**
     * This function returns the precision value in a response format that can be sent back as JSON.
     *
     * @return A Response object containing the precision value as a JSON string.
     */
    public Response resultForPrecision() {
        // Get the precision value
        Double precision = precisionResult();

        try {
            // Return the precision value as a JSON response
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "http://localhost:63342")
                    .header("Content-Type", "application/json")
                    .entity(objectMapper.writeValueAsString("Precision value :" + precision))
                    .build();
        } catch (JsonProcessingException e) {
            // Handle any JSON processing errors
            throw new RuntimeException(e);
        }
    }


}