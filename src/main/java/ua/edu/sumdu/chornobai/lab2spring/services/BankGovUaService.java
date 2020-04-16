package ua.edu.sumdu.chornobai.lab2spring.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private DateParsingService dateParsingService;
    private String api;

    @Autowired
    public BankGovUaService(HTTPRequestService httpRequestService, JacksonParsingService jacksonParsingService,
                            DateParsingService dateParsingService,
                            @Value("${api.bankgovua}") String api) {
        this.httpRequestService = httpRequestService;
        this.jacksonParsingService = jacksonParsingService;
        this.dateParsingService = dateParsingService;
        this.api = api;
    }


    final static Logger logger = Logger.getLogger(BankGovUaService.class);

    @Async
    public CompletableFuture<CurrencyValue> getResult(String date, String currency) {

        String formattedDate = dateParsingService.getFormattedDate(date);

        CurrencyValue newCurrencyValue = new CurrencyValue();

        String urlGovUa = api + formattedDate + "&amp;json";
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
        } else {
            newCurrencyValue.setBank("bank.gov.ua");
            newCurrencyValue.setMessage("bank.gov.ua didn't responded");
            logger.info("No response from bank.gov.ua");
        }
        return CompletableFuture.completedFuture(newCurrencyValue);
    }
}
