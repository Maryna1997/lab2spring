package ua.edu.sumdu.chornobai.lab2spring.controller;

import org.apache.log4j.Level;
import ua.edu.sumdu.chornobai.lab2spring.model.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.sumdu.chornobai.lab2spring.services.*;
import org.apache.log4j.Logger;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;

@RestController
@RequestMapping(path = "/apicurrency")
public class AppController {

    private PrivatbankService privatbankService;
    private BankGovUaService bankGovUaService;
    private MonobankService monobankService;
    private BestExchangeRateService bestExchangeRateService;

    final static Logger logger = Logger.getLogger(AppController.class);

    public AppController(PrivatbankService privatbankService, BankGovUaService bankGovUaService,
                         MonobankService monobankService, BestExchangeRateService bestExchangeRateService) {
        this.privatbankService = privatbankService;
        this.bankGovUaService = bankGovUaService;
        this.monobankService = monobankService;
        this.bestExchangeRateService = bestExchangeRateService;
    }

    @RequestMapping(path = "/exchangeRate/{currency}/{date}", method = RequestMethod.GET)
    public ExchangeRate getExchangeRate(@PathVariable(name = "currency") String currency,
                                  @PathVariable(name = "date") String date) {
        logger.info("New request foe get exchange rate (currency = " + currency + ", date = " + date + ")");
        ExchangeRate result = new ExchangeRate();
        result.setCurrency(currency);
        result.setPeriod(date);
        try {
            int dd = Integer.parseInt(date.substring(0, 2));
            int mm = Integer.parseInt(date.substring(3, 5));
            int yyyy = Integer.parseInt(date.substring(6, 10));
            LocalDate localDate = LocalDate.of(yyyy, mm, dd);

            ArrayList<CurrencyValue> currencyValueList = new ArrayList<>();
            if (localDate.isBefore(LocalDate.now()) || localDate.equals(LocalDate.now())) {
                privatbankService.getResult(date, currency, currencyValueList);
                bankGovUaService.getResult(date, currency, currencyValueList);
                if (localDate.equals(LocalDate.now())) {
                    monobankService.getResult(currency, date, currencyValueList);
                }
                if (currencyValueList.size() > 0)  result.setListOfValue(currencyValueList);
                else result.setMessage("Currency info not found!");
                logger.info("Response result:" + result);
            } else {
                result.setMessage("Requested date is greater than today's date. Please enter a valid date");
                logger.info("Requested date"  + date + "is greater than today's date");
            }
            return result;
        }
        catch (IndexOutOfBoundsException | NumberFormatException | DateTimeException e) {
            result.setMessage("Error: invalid date format");
            logger.log(Level.FATAL, "Exception: ", e);
            return result;
        }
    }

    @RequestMapping(path = "/bestExchangeRate/{currency}/{period}", method = RequestMethod.GET)
    public ExchangeRate getBestExchangeRate(@PathVariable(name = "currency") String currency,
                                      @PathVariable(name = "period") String period) {
        logger.info("New request for get the best exchange rate (currency = " + currency + ", period = " + period + ")");
        ExchangeRate result = new ExchangeRate();
        ArrayList<CurrencyValue> currencyValueList;
        result.setCurrency(currency);
        result.setPeriod(period);
        switch (period){
            case "year":
                currencyValueList = bestExchangeRateService.getBestExchangeRateForPeriod(LocalDate.now().plusYears(-1), currency);
                if(currencyValueList.size() > 0) result.setListOfValue(currencyValueList);
                else result.setMessage("Currency info not found!");
                return result;
            case "month":
                currencyValueList = bestExchangeRateService.getBestExchangeRateForPeriod(LocalDate.now().plusMonths(-1), currency);
                if(currencyValueList.size() > 0) result.setListOfValue(currencyValueList);
                else result.setMessage("Currency info not found!");
                return result;
            case "week":
                currencyValueList = bestExchangeRateService.getBestExchangeRateForPeriod(LocalDate.now().plusDays(-7), currency);
                if(currencyValueList.size() > 0) result.setListOfValue(currencyValueList);
                else result.setMessage("Currency info not found!");
                return result;
            default:
                logger.info("Incorrect period");
                result.setMessage("Incorrect period. Please, enter 'week', 'month' or 'year'");
                return result;
         }
    }
}
