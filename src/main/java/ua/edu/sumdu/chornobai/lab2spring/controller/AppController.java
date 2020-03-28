package ua.edu.sumdu.chornobai.lab2spring.controller;

import org.apache.log4j.Level;
import ua.edu.sumdu.chornobai.lab2spring.model.*;
import org.springframework.web.bind.annotation.*;
import ua.edu.sumdu.chornobai.lab2spring.services.*;
import org.apache.log4j.Logger;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
        logger.info("New request for get exchange rate (currency = " + currency + ", date = " + date + ")");
        ExchangeRate result = new ExchangeRate();
        ArrayList<CurrencyValue> currencyValueList = new ArrayList<>();
        result.setCurrency(currency);
        result.setPeriod(date);
        try {
            int dd = Integer.parseInt(date.substring(0, 2));
            int mm = Integer.parseInt(date.substring(3, 5));
            int yyyy = Integer.parseInt(date.substring(6, 10));
            LocalDate localDate = LocalDate.of(yyyy, mm, dd);

            if (localDate.isBefore(LocalDate.now()) || localDate.equals(LocalDate.now())) {
                CurrencyValue privatbankResult = privatbankService.getResult(date, currency).get();
                 if (privatbankResult.getBank() != null) {
                     currencyValueList.add(privatbankResult);
                 }
                CurrencyValue bankGovUaResult = bankGovUaService.getResult(date, currency).get();
                if (bankGovUaResult.getBank() != null) {
                    currencyValueList.add(bankGovUaResult);
                }
                if (localDate.equals(LocalDate.now())) {
                    CurrencyValue monobankResult = monobankService.getResult(currency, date).get();
                    if (monobankResult.getBank() != null) {
                        currencyValueList.add(monobankResult);
                    }
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
        catch (IndexOutOfBoundsException | NumberFormatException | DateTimeException  e) {
            result.setMessage("Error: invalid date format");
            logger.log(Level.FATAL, "Exception: ", e);
            return result;
        }
        catch (InterruptedException | ExecutionException  e){
            result.setMessage("Error: try again!");
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
                try {
                    currencyValueList = bestExchangeRateService.getBestExchangeRateForPeriod(LocalDate.now().plusYears(-1),
                            currency).get();
                    if(currencyValueList.size() > 0) result.setListOfValue(currencyValueList);
                    else result.setMessage("Currency info not found!");
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.FATAL, "Exception: ", e);
                }
                break;
            case "month":
                try {
                    currencyValueList = bestExchangeRateService.getBestExchangeRateForPeriod(LocalDate.now().plusMonths(-1),
                            currency).get();
                    if(currencyValueList.size() > 0) result.setListOfValue(currencyValueList);
                    else result.setMessage("Currency info not found!");
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.FATAL, "Exception: ", e);
                }
                break;
            case "week":
                try {
                    currencyValueList = bestExchangeRateService.getBestExchangeRateForPeriod(LocalDate.now().plusDays(-7),
                            currency).get();
                    if(currencyValueList.size() > 0) result.setListOfValue(currencyValueList);
                    else result.setMessage("Currency info not found!");
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.FATAL, "Exception: ", e);
                }
                break;
            default:
                logger.info("Incorrect period");
                result.setMessage("Incorrect period. Please, enter 'week', 'month' or 'year'");
                break;
         }
        logger.info("Response result: " + result);
        return result;
    }
}
