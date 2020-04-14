package ua.edu.sumdu.chornobai.lab2spring.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.chornobai.lab2spring.model.CurrencyGovUa;
import ua.edu.sumdu.chornobai.lab2spring.model.CurrencyValue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Service
public class MinMaxExchangeRateService {
    private HTTPRequestService httpRequestService;
    private JacksonParsingService jacksonParsingService;
    private DateParsingService dateParsingService;

    @Autowired
    public MinMaxExchangeRateService(HTTPRequestService httpRequestService, JacksonParsingService jacksonParsingService,
                                     DateParsingService dateParsingService) {
        this.httpRequestService = httpRequestService;
        this.jacksonParsingService = jacksonParsingService;
        this.dateParsingService = dateParsingService;
    }


    final static Logger logger = Logger.getLogger(MinMaxExchangeRateService.class);

    @Async
    public CompletableFuture<ArrayList<CurrencyValue>> getMinMaxExchangeRateForPeriod(String value, LocalDate startDay,
                                                                                      String currency) {

        ArrayList<CurrencyValue> currencyValueList = new ArrayList<>();
        float minmaxCurrencyValue;

        for (LocalDate date = startDay; date.isBefore(LocalDate.now()); date = date.plusDays(1)) {
            String stringDate = dateParsingService.getStringDate(date);

            String formattedDate = dateParsingService.getFormattedDate(stringDate);
            String urlGovUa = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?date="
                    + formattedDate + "&amp;json";
            String resultGovUa = httpRequestService.getJSONResult(urlGovUa);

            if (!(resultGovUa.equals(""))) {
                List<CurrencyGovUa> currencyGovUa = jacksonParsingService.parseJSON(resultGovUa);

                for (CurrencyGovUa cur : currencyGovUa
                ) {
                    if (currency.equals(cur.getCc())) {
                        CurrencyValue newCurrencyValue = new CurrencyValue();
                        newCurrencyValue.setBank("bank.gov.ua");
                        newCurrencyValue.setDate(cur.getExchangedate());
                        newCurrencyValue.setSaleRate(cur.getRate());
                        newCurrencyValue.setPurchaseRate(cur.getRate());
                        currencyValueList.add(newCurrencyValue);
                    }
                }
            }
            else  {
                CurrencyValue newCurrencyValue = new CurrencyValue();
                newCurrencyValue.setBank("bank.gov.ua");
                newCurrencyValue.setMessage("bank.gov.ua didn't responded");
                currencyValueList.add(newCurrencyValue);
                logger.info("No response from bank.gov.ua");
            }
        }
        if (currencyValueList.size() > 0) {
            if(value.equals("max")) minmaxCurrencyValue = getMaxCurrencyValue(currencyValueList);
            else minmaxCurrencyValue = getMinCurrencyValue(currencyValueList);

            Stream<CurrencyValue> stream = new ArrayList<>(currencyValueList.subList(0, currencyValueList.size())).stream();
            stream.filter(cur -> cur.getSaleRate() != minmaxCurrencyValue).forEach(currencyValueList:: remove);
        }

        return CompletableFuture.completedFuture(currencyValueList);
    }

    public float getMaxCurrencyValue(ArrayList<CurrencyValue> currencyValueList) {
        float maxCurrencyValue = currencyValueList.get(0).getSaleRate();
        for (CurrencyValue cur: currencyValueList
        ) {
            if(cur.getSaleRate() > maxCurrencyValue) maxCurrencyValue = cur.getSaleRate();
        }
        logger.info("Max currency value = " + maxCurrencyValue);
        return maxCurrencyValue;
    }

    public float getMinCurrencyValue(ArrayList<CurrencyValue> currencyValueList) {
        float minCurrencyValue = currencyValueList.get(0).getSaleRate();
        for (CurrencyValue cur: currencyValueList
        ) {
            if(cur.getSaleRate() < minCurrencyValue) minCurrencyValue = cur.getSaleRate();
        }
        logger.info("Min currency value = " + minCurrencyValue);
        return minCurrencyValue;
    }
}
