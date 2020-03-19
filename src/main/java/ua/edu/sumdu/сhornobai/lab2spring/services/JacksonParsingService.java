package ua.edu.sumdu.сhornobai.lab2spring.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.сhornobai.lab2spring.model.CurrencyGovUa;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class JacksonParsingService {

    final static Logger logger = Logger.getLogger(JacksonParsingService.class);

    public List<CurrencyGovUa> parseJSON (String resultGovUa)  {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<CurrencyGovUa> result =  Arrays.asList(objectMapper.readValue(resultGovUa, CurrencyGovUa[].class));
            logger.info("Parsing by JacksonParsingService");
            return result;
        } catch (IOException e) {
            logger.log(Level.FATAL, "Exception: ", e);
            return null;
        }
    }
}
