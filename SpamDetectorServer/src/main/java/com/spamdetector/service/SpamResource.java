package com.spamdetector.service;

import com.spamdetector.domain.TestFile;
import com.spamdetector.util.SpamDetector;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.File;
import java.util.List;

import jakarta.ws.rs.core.Response;

@Path("/spam")
public class SpamResource {

    SpamDetector detector = new SpamDetector();



    SpamResource(){
//        TODO: load resources, train and test to improve performance on the endpoint calls
        System.out.print("Training and testing the model, please wait");
        //detector.wordFrequency()

//      TODO: call  this.trainAndTest();
    }
    @GET
    @Produces("application/json")
    public Response getSpamResults() {
//       TODO: return the test results list of TestFile, return in a Response object

        return detector.resultForGetSpam();
    }

    @GET
    @Path("/accuracy")
    @Produces("application/json")
    public Response getAccuracy() {
//      TODO: return the accuracy of the detector, return in a Response object

        return detector.resultForAccuracy();
    }

    @GET
    @Path("/precision")
    @Produces("application/json")
    public Response getPrecision() {
       //      TODO: return the precision of the detector, return in a Response object

        return detector.resultForPrecision();
    }


}