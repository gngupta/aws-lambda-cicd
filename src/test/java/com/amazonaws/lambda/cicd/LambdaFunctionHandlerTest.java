package com.amazonaws.lambda.cicd;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.model.GetObjectRequest;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class LambdaFunctionHandlerTest {

    private final String CONTENT_TYPE = "GORAKH";
  
    @Captor
    private ArgumentCaptor<GetObjectRequest> getObjectRequest;

   
    private Context createContext() {
        TestContext ctx = new TestContext();
        ctx.setFunctionName("LambdaFunctionHandler");
        return ctx;
    }

    @Test
    public void testLambdaFunctionHandler() {
        LambdaFunctionHandler handler = new LambdaFunctionHandler();
        Context ctx = createContext();
        String output = handler.handleRequest("gorakh", ctx);
        Assert.assertEquals(CONTENT_TYPE, output);
    }
}
