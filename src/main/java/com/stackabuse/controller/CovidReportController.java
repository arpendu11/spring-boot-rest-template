package com.stackabuse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/covid")
public class CovidReportController {

	@Autowired
    @Qualifier("rapidApiRestTemplate")
    private RestTemplate restTemplate;
	
	@GetMapping("/getAllLatest")
	public ResponseEntity<String> getAllCovidCount() {		
		return restTemplate.getForEntity("https://covid-19-data.p.rapidapi.com/totals?format=json",
				String.class);
	}
}
