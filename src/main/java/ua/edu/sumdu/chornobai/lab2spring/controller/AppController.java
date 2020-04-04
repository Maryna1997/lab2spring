package ua.edu.sumdu.chornobai.lab2spring.controller;

import org.apache.log4j.Level;
import ua.edu.sumdu.chornobai.lab2spring.model.*;
import org.springframework.web.bind.annotation.*;
import ua.edu.sumdu.chornobai.lab2spring.services.*;
import org.apache.log4j.Logger;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "/apicurrency")
public class AppController {

    private PrivatbankService privatbankService;
    private BankGovUaService bankGovUaService;
    private MonobankService monobankService;
    private MinMaxExchangeRateService minMaxExchangeRateService;

    final static Logger logger = Logger.getLogger(AppController.class);

    public AppController(PrivatbankService privatbankService, BankGovUaService bankGovUaService,
                         MonobankService monobankService, MinMaxExchangeRateService minMaxExchangeRateService) {
        this.privatbankService = privatbankService;
        this.bankGovUaService = bankGovUaService;
        this.monobankService = monobankService;
        this.minMaxExchangeRateService = minMaxExchangeRateService;
    }

    @RequestMapping(path = "/exchangeRate/{currency}/{date}", method = RequestMethod.GET)
    public ExchangeRate getExchangeRate(@PathVariable(name = "currency") String currency,
                                  @PathVariable(name = "date") String date) {
        logger.info("New request for get exchange rate (currency = " + currency + ", date = " + date + ")");
        ExchangeRate result = new ExchangeRate();
        ArrayList<CurrencyValue> currencyValueList = new ArrayList<>();
        List<CurrencyValue> combinedResult;
        result.setCurrency(currency);
        result.setPeriod(date);
        try {
            int dd = Integer.parseInt(date.substring(0, 2));
            int mm = Integer.parseInt(date.substring(3, 5));
            int yyyy = Integer.parseInt(date.substring(6, 10));
            LocalDate localDate = LocalDate.of(yyyy, mm, dd);

            if (localDate.isBefore(LocalDate.now()) || localDate.equals(LocalDate.now())) {
                CompletableFuture<CurrencyValue> privatbankResult = privatbankService.getResult(date, currency);
                CompletableFuture<CurrencyValue> bankGovUaResult = bankGovUaService.getResult(date, currency);

                if (localDate.equals(LocalDate.now())) {
                    CompletableFuture<CurrencyValue> monobankResult = monobankService.getResult(currency, date);
                    combinedResult = Stream.of(privatbankResult, bankGovUaResult, monobankResult)
                            .map(CompletableFuture::join).collect(Collectors.toList());
                }
                else {
                    combinedResult = Stream.of(privatbankResult, bankGovUaResult).map(CompletableFuture::join)
                            .collect(Collectors.toList());
                }

                for (CurrencyValue cur: combinedResult
                     ) {
                    if (cur.getBank() != null) {
                       currencyValueList.add(cur);
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
    }


    @RequestMapping(path = "/exchangeRate/{value}/{currency}/{period}", method = RequestMethod.GET)
    public ExchangeRate getMinMaxExchangeRate(@PathVariable(name = "value") String value,
                                            @PathVariable(name = "currency") String currency,
                                            @PathVariable(name = "period") String period) {
        logger.info("New request for get the " + value + " exchange rate (currency = " + currency + ", " +
                "period = " + period + ")");
        ExchangeRate result = new ExchangeRate();
        ArrayList<CurrencyValue> currencyValueList;
        result.setCurrency(currency);
        result.setPeriod(period);
        if(value.equals("max") || value.equals("min")){
            result.setMessage(value + " exchange rate");
            switch (period){
                case "year":
                    try {
                        currencyValueList = minMaxExchangeRateService.getMinMaxExchangeRateForPeriod(value,
                                LocalDate.now().plusYears(-1), currency).get();
                        if(currencyValueList.size() > 0) result.setListOfValue(currencyValueList);
                        else result.setMessage("Currency info not found!");
                    } catch (InterruptedException | ExecutionException e) {
                        logger.log(Level.FATAL, "Exception: ", e);
                    }
                    break;
                case "month":
                    try {
                        currencyValueList = minMaxExchangeRateService.getMinMaxExchangeRateForPeriod(value,
                                LocalDate.now().plusMonths(-1), currency).get();
                        if(currencyValueList.size() > 0) result.setListOfValue(currencyValueList);
                        else result.setMessage("Currency info not found!");
                    } catch (InterruptedException | ExecutionException e) {
                        logger.log(Level.FATAL, "Exception: ", e);
                    }
                    break;
                case "week":
                    try {
                        currencyValueList = minMaxExchangeRateService.getMinMaxExchangeRateForPeriod(value,
                                LocalDate.now().plusDays(-7), currency).get();
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
        }
        else result.setMessage("Сan't get the " + value + " value of the exchange rate. Only 'min' or 'max'");
        logger.info("Response result: " + result);
        return result;
    }

    @RequestMapping(path = "/currentExchangeRate/{bank}/{currency}", method = RequestMethod.GET)
    public ExchangeRate getCurrentExchangeRate(@PathVariable(name = "bank") String bank,
                                        @PathVariable(name = "currency") String currency) {
        LocalDate currentDate = LocalDate.now();
        String stringDate = (currentDate.getDayOfMonth() <10? "0" + currentDate.getDayOfMonth() : currentDate.getDayOfMonth() )
                + "." + (currentDate.getMonthValue() < 10 ? "0" + currentDate.getMonthValue(): currentDate.getMonthValue())
                + "." + currentDate.getYear();
        ExchangeRate result =  getExchangeRate(currency, stringDate);
        ArrayList<CurrencyValue> resultList = result.getListOfValue();
        ArrayList<CurrencyValue> newResultList = new ArrayList<>();
        if (resultList != null) {
            for (CurrencyValue curValue:  resultList
            ) {
                if (bank.equals(curValue.getBank())) newResultList.add(curValue);
            }
            if (newResultList.size() == 0) result.setMessage("Bank info not found!");
        }
        else result.setMessage("Currency info not found!");
        result.setListOfValue(newResultList);
        return result;
    }
}
