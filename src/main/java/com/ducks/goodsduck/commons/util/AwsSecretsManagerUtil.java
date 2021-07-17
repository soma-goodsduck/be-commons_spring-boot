//package com.ducks.goodsduck.commons.util;
//
//import com.amazonaws.services.secretsmanager.AWSSecretsManager;
//import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
//import com.amazonaws.services.secretsmanager.model.*;
//import lombok.extern.slf4j.Slf4j;
//import org.json.JSONObject;
//
//import java.util.Base64;
//
//@Slf4j
//public class AwsSecretsManagerUtil {
//
//    public static JSONObject getSecret() {
//        String secretName = "dev/secret/goodsduck/backend";
//        String region = "ap-northeast-2";
//
//        // Create a Secrets Manager client
//        AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
//                .withRegion(region)
//                .build();
//
//        // In this sample we only handle the specific exceptions for the 'GetSecretValue' API.
//        // See https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
//        // We rethrow the exception by default.
//
//        String secret, decodedBinarySecret;
//        String jsonStringOfAwsSecrets;
//        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
//                .withSecretId(secretName);
//        GetSecretValueResult getSecretValueResult = null;
//
//        JSONObject jsonObject = new JSONObject();
//
//        try {
//            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
//        } catch (DecryptionFailureException e) {
//            // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
//            // Deal with the exception here, and/or rethrow at your discretion.
//            log.debug("Aws Credentials not found : ", e.getMessage());
//            return jsonObject;
////            throw e;
//        } catch (InternalServiceErrorException e) {
//            // An error occurred on the server side.
//            // Deal with the exception here, and/or rethrow at your discretion.
//            log.debug("Aws Credentials not found : ", e.getMessage());
//            return jsonObject;
////            throw e;
//        } catch (InvalidParameterException e) {
//            // You provided an invalid value for a parameter.
//            // Deal with the exception here, and/or rethrow at your discretion.
//            log.debug("Aws Credentials not found : ", e.getMessage());
//            return jsonObject;
////            throw e;
//        } catch (InvalidRequestException e) {
//            // You provided a parameter value that is not valid for the current state of the resource.
//            // Deal with the exception here, and/or rethrow at your discretion.
//            log.debug("Aws Credentials not found : ", e.getMessage());
//            return jsonObject;
////            throw e;
//        } catch (ResourceNotFoundException e) {
//            // We can't find the resource that you asked for.
//            // Deal with the exception here, and/or rethrow at your discretion.
//            log.debug("Aws Credentials not found : ", e.getMessage());
//            return jsonObject;
////            throw e;
//        } catch (Exception e) {
//            log.debug("Aws Credentials not found : ", e.getMessage());
//            return jsonObject;
//        }
//
//        // Decrypts secret using the associated KMS CMK.
//        // Depending on whether the secret is a string or binary, one of these fields will be populated.
//        if (getSecretValueResult.getSecretString() != null) {
//            secret = getSecretValueResult.getSecretString();
//            jsonStringOfAwsSecrets = secret;
//        }
//        else {
//            decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
//            jsonStringOfAwsSecrets = decodedBinarySecret;
//        }
//
//        // Your code goes here.
//        jsonObject = new JSONObject(jsonStringOfAwsSecrets);
//        return jsonObject;
//    }
//}
