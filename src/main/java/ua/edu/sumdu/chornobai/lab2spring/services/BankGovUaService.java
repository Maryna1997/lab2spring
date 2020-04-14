package ua.edu.sumdu.chornobai.lab2spring.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.chornobai.lab2spring.model.CurrencyGovUa;
import ua.edu.sumdu.chornobai.lab2spring.model.CurrencyValue;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class BankGovUaService {
    private HTTPRequestService httpRequestService;
    private JacksonParsingService jacksonParsingService;

    @Autowired
    public BankGovUaService(HTTPRequestService httpRequestService, JacksonParsingService jacksonParsingService) {
        this.httpRequestService = httpRequestService;
        this.jacksonParsingService = jacksonParsingService;
    }

    final static Logger logger = Logger.getLogger(BankGovUaService.class);

    @Async
    public CompletableFuture<CurrencyValue> getResult(String date, String currency) {
        String day = date.substring(0, 2);
        String month = date.substring(3, 5);
        String year = date.substring(6, 10);
        String formattedDate = year + month + day;

        CurrencyValue newCurrencyValue = new CurrencyValue();

        String urlGovUa = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?date="
                + formattedDate + "&amp;json";
        String resultGovUa = httpRequestService.getJSONResult(urlGovUa);

        if (!(resultGovUa.equals(""))) {
            List<CurrencyGovUa> currencyGovUa = jacksonParsingService.parseJSON(resultGovUa);

            for (CurrencyGovUa cur : currencyGovUa
            ) {
                if (currency.equals(cur.getCc())) {
                    newCurrencyValue.setBank("bank.gov.ua");
                    newCurrencyValue.setDate(date);
                    newCurrencyValue.setSaleRate(cur.getRate());
                    newCurrencyValue.setPurchaseRate(cur.getRate());
                    logger.info("Response from bank.gov.ua: " + newCurrencyValue);
                }
            }
        }
        else {
            newCurrencyValue.setBank("bank.gov.ua");
            newCurrencyValue.setMessage("bank.gov.ua didn't responded");
            logger.info("No response from bank.gov.ua");
        }
        return CompletableFuture.completedFuture(newCurrencyValue);
    }
}
