package ua.edu.sumdu.сhornobai.lab2spring.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.сhornobai.lab2spring.model.CurrencyGovUa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class JacksonParsingService {
    public List<CurrencyGovUa> parseJSON (String resultGovUa) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return Arrays.asList(objectMapper.readValue(resultGovUa, CurrencyGovUa[].class));
    }
}
