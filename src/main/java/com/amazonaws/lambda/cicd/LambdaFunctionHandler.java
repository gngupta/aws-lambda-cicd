package com.amazonaws.lambda.cicd;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler implements RequestHandler<String, String> {
    @Override
    public String handleRequest(String input, Context context) {
    	 LambdaLogger logger = context.getLogger();
         logger.log("response v2 : " + input);
         return input.toUpperCase();
    }
}