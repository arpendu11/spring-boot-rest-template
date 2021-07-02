package com.stackabuse.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class HttpClientRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientRequestInterceptor.class);
	
	private final String headerName;

    private final String headerValue;

    public HttpClientRequestInterceptor(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {    	
    	// Add Auth Headers
    	request.getHeaders().set(headerName, headerValue);
        
        // Add logger info settings
        logRequestDetails(request);

        return execution.execute(request, body);
    }

    // Adding custom loggers
    private void logRequestDetails(HttpRequest request) {
        LOGGER.info("Request Headers: {}", request.getHeaders());
        LOGGER.info("Request Method: {}", request.getMethod());
        LOGGER.info("Request URI: {}", request.getURI());
    }
}
