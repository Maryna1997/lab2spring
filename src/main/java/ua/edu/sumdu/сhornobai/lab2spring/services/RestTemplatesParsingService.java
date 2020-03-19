package ua.edu.sumdu.сhornobai.lab2spring.services;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.edu.sumdu.сhornobai.lab2spring.model.CurrencyMonobank;

import java.util.Objects;

@Service
public class RestTemplatesParsingService {

    final static Logger logger = Logger.getLogger(RestTemplatesParsingService.class);

    public CurrencyMonobank[] parseJSON (){
        RestTemplate restTemplate = new RestTemplate();
        CurrencyMonobank[] result =  Objects.requireNonNull(restTemplate.getForObject("https://api.monobank.ua/bank/currency",
                CurrencyMonobank[].class));
        logger.info("Parsing by RestTemplatesParsingService");
        return result;
    }
}
