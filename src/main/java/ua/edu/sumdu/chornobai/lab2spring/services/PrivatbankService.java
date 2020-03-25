package ua.edu.sumdu.chornobai.lab2spring.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.chornobai.lab2spring.model.CurrencyPrivatbank;
import ua.edu.sumdu.chornobai.lab2spring.model.CurrencyValue;

import java.util.ArrayList;

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

    //@Async
    public void getResult(String date, String currency, ArrayList<CurrencyValue> currencyValueList) {
        String urlPrivatbank = "https://api.privatbank.ua/p24api/exchange_rates?json&date=" + date;
        String resultPrivatbank = httpRequestService.getJSONResult(urlPrivatbank);
        if (!(resultPrivatbank.equals(""))) {
            ArrayList<CurrencyPrivatbank> listCurrencyPrivatbank = new ArrayList<>();
            orgJSONParsingService.parseJSON(resultPrivatbank, date, listCurrencyPrivatbank);
            for (CurrencyPrivatbank cur : listCurrencyPrivatbank) {
                if (currency.equals(cur.getTitle())) {
                    CurrencyValue newCurrencyValue = new CurrencyValue();
                    newCurrencyValue.setBank("Privatbank");
                    newCurrencyValue.setDate(date);
                    newCurrencyValue.setSaleRate(cur.getSaleRate());
                    newCurrencyValue.setPurchaseRate(cur.getPurchaseRate());
                    currencyValueList.add(newCurrencyValue);
                    logger.info("Add new element to listCurrencyPrivatbank:" + newCurrencyValue);
                }
            }
        }
        else {
            logger.info("No response from Privatbank");
            CurrencyValue newCurrencyValue = new CurrencyValue();
            newCurrencyValue.setBank("Privatbank");
            newCurrencyValue.setMessage("Privatbank didn't responded");
            currencyValueList.add(newCurrencyValue);
        }
    }
}
