package ua.edu.sumdu.chornobai.lab2spring.services;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.chornobai.lab2spring.model.CurrencyMonobank;
import ua.edu.sumdu.chornobai.lab2spring.model.CurrencyValue;

import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
public class MonobankService {
    private RestTemplatesParsingService restTemplatesParsingService;

    @Autowired
    public MonobankService(RestTemplatesParsingService restTemplatesParsingService) {
        this.restTemplatesParsingService = restTemplatesParsingService;
    }
    final static Logger logger = Logger.getLogger(MonobankService.class);

    @Async
    public CompletableFuture<CurrencyValue> getResult(String currency, String date){
        CurrencyMonobank[] currencyMonobank = restTemplatesParsingService.parseJSON();
        CurrencyValue newCurrencyValue = new CurrencyValue();
        for (CurrencyMonobank cur : currencyMonobank
        ) {
            if (getCurrencyDigitalCode(currency) == cur.getCurrencyCodeA() && cur.getCurrencyCodeB() == 980) {
                newCurrencyValue.setBank("Monobank");
                newCurrencyValue.setDate(date);
                newCurrencyValue.setSaleRate(cur.getRateSell());
                newCurrencyValue.setPurchaseRate(cur.getRateBuy());
                logger.info("Response from Monobank: " + newCurrencyValue);
            }
        }
        return CompletableFuture.completedFuture(newCurrencyValue);
    }

    public long getCurrencyDigitalCode(String currency) {
        long currencyCode = 0;
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(".\\src\\main\\files\\iso4217.json")) {
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray jsonArray = (JSONArray) jsonObject.get("info");
            for (Object jsonObj: jsonArray
                 ) {
                JSONObject jsonInfo = (JSONObject) jsonObj;
                if(currency.equals(jsonInfo.get("literalCode"))){
                    currencyCode =  (long) jsonInfo.get("digitalCode");
                }
            }
        } catch (ParseException | IOException | NullPointerException e) {
            logger.log(Level.FATAL, "Exception: ", e);
        }
        return currencyCode;
    }
}
