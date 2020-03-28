package ua.edu.sumdu.chornobai.lab2spring.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.chornobai.lab2spring.model.CurrencyPrivatbank;
import ua.edu.sumdu.chornobai.lab2spring.model.CurrencyValue;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class PrivatbankService {
     private HTTPRequestService httpRequestService;
     private OrgJSONParsingService orgJSONParsingService;

    @Autowired
    public PrivatbankService(HTTPRequestService httpRequestService, OrgJSONParsingService orgJSONParsingService) {
        this.httpRequestService = httpRequestService;
        this.orgJSONParsingService = orgJSONParsingService;
    }

    final static Logger logger = Logger.getLogger(PrivatbankService.class);

    @Async
    public CompletableFuture<CurrencyValue> getResult(String date, String currency) {
        String urlPrivatbank = "https://api.privatbank.ua/p24api/exchange_rates?json&date=" + date;
        String resultPrivatbank = httpRequestService.getJSONResult(urlPrivatbank);
        CurrencyValue newCurrencyValue = new CurrencyValue();
        if (!(resultPrivatbank.equals(""))) {
            ArrayList<CurrencyPrivatbank> listCurrencyPrivatbank = new ArrayList<>();
            orgJSONParsingService.parseJSON(resultPrivatbank, date, listCurrencyPrivatbank);
            for (CurrencyPrivatbank cur : listCurrencyPrivatbank) {
                if (currency.equals(cur.getTitle())) {
                    newCurrencyValue.setBank("Privatbank");
                    newCurrencyValue.setDate(date);
                    newCurrencyValue.setSaleRate(cur.getSaleRate());
                    newCurrencyValue.setPurchaseRate(cur.getPurchaseRate());
                    logger.info("Response from Privatbank:" + newCurrencyValue);
                }
            }
        }
        else {
            logger.info("No response from Privatbank");
            newCurrencyValue.setBank("Privatbank");
            newCurrencyValue.setMessage("Privatbank didn't responded");
        }
        return CompletableFuture.completedFuture(newCurrencyValue);
    }
}
