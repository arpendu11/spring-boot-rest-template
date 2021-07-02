package com.stackabuse.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Value("${application.keystore.path}")
	private String keystorePath;

	@Value("${application.keystore.type}")
	private String keystoreType;

	@Value("${application.keystore.password}")
	private String keystorePassword;

	@Value("${application.truststore.path}")
	private String truststorePath;

	@Value("${application.truststore.type}")
	private String truststoreType;

	@Value("${application.truststore.password}")
	private String truststorePassword;

	@Value("${application.protocol}")
	private String protocol;

	@Value("${api.rapid-api.host}")
	private String rapidApiHost;

	@Value("${api.rapid-api.key}")
	private String rapidApiKey;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.setConnectTimeout(Duration.ofMillis(60000))
				.setReadTimeout(Duration.ofMillis(60000))
				.build();
	}

	@Bean(name = "rapidApiRestTemplate")
	public RestTemplate rapidApiRestTemplate()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();

		// Connect timeout
		clientHttpRequestFactory.setConnectTimeout(60000);

		// Read timeout
		clientHttpRequestFactory.setReadTimeout(60000);

		RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

		// Interceptor section
		List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
		if (CollectionUtils.isEmpty(interceptors)) {
			interceptors = new ArrayList<ClientHttpRequestInterceptor>();
		}
		interceptors.add(new HttpClientRequestInterceptor("x-rapidapi-key", rapidApiKey));
		interceptors.add(new HttpClientRequestInterceptor("x-rapidapi-host", rapidApiHost));
		restTemplate.setInterceptors(interceptors);

		return restTemplate;
		
	}

	@Bean(name = "mutualAuthRestTemplate")
	public RestTemplate mutualAuthRestTemplate() throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException, KeyManagementException {

		// Load Keystore
		final KeyStore keystore = KeyStore.getInstance(keystoreType);
		try (InputStream in = new FileInputStream(keystorePath)) {
			keystore.load(in, keystorePassword.toCharArray());
		}

		// Load Truststore
		final KeyStore truststore = KeyStore.getInstance(truststoreType);
		try (InputStream in = new FileInputStream(truststorePath)) {
			truststore.load(in, truststorePassword.toCharArray());
		}

		// Build SSLConnectionSocket to verify certificates
		final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(new SSLContextBuilder()
				.loadTrustMaterial(truststore, new TrustSelfSignedStrategy()).setProtocol(protocol).build(),
				new HostnameVerifier() {
					HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

					@Override
					public boolean verify(String hostname, SSLSession session) {
						return hostnameVerifier.verify(hostname, session);
					}
				});

		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpclient));
	}
}
