package ua.edu.sumdu.chornobai.lab2spring.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.edu.sumdu.chornobai.lab2spring.model.CurrencyMonobank;

import java.util.Objects;

@Service
public class RestTemplatesParsingService {

    private String api;

    public RestTemplatesParsingService(@Value("${api.monobank}") String api) {
        this.api = api;
    }

    final static Logger logger = Logger.getLogger(RestTemplatesParsingService.class);

    public CurrencyMonobank[] parseJSON() {
        RestTemplate restTemplate = new RestTemplate();
        CurrencyMonobank[] result = Objects.requireNonNull(restTemplate.getForObject(api, CurrencyMonobank[].class));
        logger.info("Parsing by RestTemplatesParsingService");
        return result;
    }
}
