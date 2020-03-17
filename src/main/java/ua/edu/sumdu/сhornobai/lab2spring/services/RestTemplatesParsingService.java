package ua.edu.sumdu.сhornobai.lab2spring.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.edu.sumdu.сhornobai.lab2spring.model.CurrencyMonobank;

import java.util.Objects;

@Service
public class RestTemplatesParsingService {
    public CurrencyMonobank[] parseJSON (){
        RestTemplate restTemplate = new RestTemplate();
        return Objects.requireNonNull(restTemplate.getForObject("https://api.monobank.ua/bank/currency", CurrencyMonobank[].class));
    }
}
