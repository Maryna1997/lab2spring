package ua.edu.sumdu.сhornobai.lab2spring.controller;

import org.apache.log4j.Level;
import ua.edu.sumdu.сhornobai.lab2spring.model.CurrencyPrivatbank;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.sumdu.сhornobai.lab2spring.model.CurrencyGovUa;
import ua.edu.sumdu.сhornobai.lab2spring.model.CurrencyMonobank;
import ua.edu.sumdu.сhornobai.lab2spring.services.HTTPRequestService;
import ua.edu.sumdu.сhornobai.lab2spring.services.JacksonParsingService;
import ua.edu.sumdu.сhornobai.lab2spring.services.OrgJSONParsingService;
import ua.edu.sumdu.сhornobai.lab2spring.services.RestTemplatesParsingService;
import org.apache.log4j.Logger;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


@RestController
@RequestMapping(path = "/apicurrency")
public class AppController {

    public HTTPRequestService httpRequestService;
    public OrgJSONParsingService orgJSONParsingService;
    public JacksonParsingService jacksonParsingService;
    public RestTemplatesParsingService restTemplatesParsingService;

    final static Logger logger = Logger.getLogger(AppController.class);

    public AppController(HTTPRequestService httpRequestService, OrgJSONParsingService orgJSONParsingService,
                         JacksonParsingService jacksonParsingService,
                         RestTemplatesParsingService restTemplatesParsingService) {
        this.httpRequestService = httpRequestService;
        this.orgJSONParsingService = orgJSONParsingService;
        this.jacksonParsingService = jacksonParsingService;
        this.restTemplatesParsingService = restTemplatesParsingService;
    }

    @RequestMapping(path = "/exchangeRate/{currency}/{date}", method = RequestMethod.GET)
    //@Async("threadPoolTaskExecutor")
    public String getExchangeRate(@PathVariable(name = "currency") String currency,
                                  @PathVariable(name = "date") String date) {
        logger.info("New request foe get exchange rate (currency = " + currency + ", date = " + date + ")");
        try {
            String day = date.substring(0, 2);
            String month = date.substring(3, 5);
            String year = date.substring(6, 10);
            String formattedDate = year + month + day;
            int dd = Integer.parseInt(day);
            int mm = Integer.parseInt(month);
            int yyyy = Integer.parseInt(year);
            LocalDate localDate = LocalDate.of(yyyy, mm, dd);

            int currencyCode = 0;
            StringBuilder responseResult = new StringBuilder();
            if (localDate.isBefore(LocalDate.now()) || localDate.equals(LocalDate.now())) {

                String urlPrivatbank = "https://api.privatbank.ua/p24api/exchange_rates?json&date=" + date;
                String resultPrivatbank = httpRequestService.getJSONResult(urlPrivatbank);
                if (!(resultPrivatbank.equals(""))) {
                    ArrayList<CurrencyPrivatbank> listCurrencyPrivatbank = new ArrayList<>();
                    orgJSONParsingService.parseJSON(resultPrivatbank, date, currency, listCurrencyPrivatbank);

                    for (CurrencyPrivatbank cur : listCurrencyPrivatbank
                    ) {
                        responseResult.append(" - ").append("Privatbank: ").append(" ( PurchaseRate: ")
                                .append(cur.getPurchaseRate()).append(" - SaleRate: ").append(cur.getSaleRate()).append(") ");
                    }
                }
                else {
                    logger.info("No response from Prinatbank");
                    responseResult.append(" -Privatbank didn't responded ");
                }

                //jackson
                String urlGovUa = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?date="
                        + formattedDate + "&amp;json";
                String resultGovUa = httpRequestService.getJSONResult(urlGovUa);

                if (!(resultGovUa.equals(""))) {
                    List<CurrencyGovUa> currencyGovUa = jacksonParsingService.parseJSON(resultGovUa);

                    for (CurrencyGovUa cur : currencyGovUa
                    ) {
                        if (currency.equals(cur.getCc())) {
                            responseResult.append(" - ").append(" bank.gov.ua: ").append(" (Rate: ")
                                    .append(cur.getRate()).append(") ");
                            currencyCode = Integer.parseInt(cur.getR030());
                        }
                    }
                }
                else {
                    logger.info("No response from bank.gov.ua");
                    responseResult.append(" -bank.gov.ua didn't responded ");
                }

                //RestTemplates
                if (localDate.equals(LocalDate.now())) {
                    CurrencyMonobank[] currencyMonobank = restTemplatesParsingService.parseJSON();

                    for (CurrencyMonobank cur : currencyMonobank
                    ) {
                        if (currencyCode == cur.getCurrencyCodeA() && cur.getCurrencyCodeB() == 980) {
                            responseResult.append(" - ").append(" Monobank:").append(" (PurchaseRate: ")
                                    .append(cur.getRateBuy()).append(" - SaleRate: ").append(cur.getRateSell()).append(") ");
                        }
                    }
                }
                String returnMessage = responseResult.toString().equals("") ? "Currency info not found!"
                        : responseResult.toString();
                logger.info("Response result:" + returnMessage);
                return currency + " (" + date + " ): " + returnMessage;
            } else {
                logger.info("Requested date"  + date + "is greater than today's date");
                return "Requested date is greater than today's date. Please enter a valid date";
            }
        }
        catch (IndexOutOfBoundsException | NumberFormatException | DateTimeException e) {
            logger.log(Level.FATAL, "Exception: ", e);
            return "Error: invalid date format";
        }
    }

    @RequestMapping(path = "/bestExchangeRate/{currency}/{period}", method = RequestMethod.GET)
    //@Async("threadPoolTaskExecutor")
    public String getBestExchangeRate(@PathVariable(name = "currency") String currency,
                                      @PathVariable(name = "period") String period) {
        logger.info("New request for get the best exchange rate (currency = " + currency + ", period = " + period + ")");
        switch (period){
            case "year":
                return getBestExchangeRateForPeriod(LocalDate.now().plusYears(-1), currency, period);
            case "month":
                return getBestExchangeRateForPeriod(LocalDate.now().plusMonths(-1), currency, period);
            case "week":
                return getBestExchangeRateForPeriod(LocalDate.now().plusDays(-7), currency, period);
            default:
                logger.info("Incorrect period");
                return "Incorrect period. Please, enter 'week', 'month' or 'year'";
         }
    }

    public String getBestExchangeRateForPeriod(LocalDate startDay, String currency, String period) {

        StringBuilder responseResult = new StringBuilder();
        ArrayList<CurrencyGovUa> listCurrencyGovUa = new ArrayList<>();

        for (LocalDate date = startDay; date.isBefore(LocalDate.now()); date = date.plusDays(1)) {
            String stringDate = (date.getDayOfMonth() <10? "0" + date.getDayOfMonth() : date.getDayOfMonth() ) + "."
                    + (date.getMonthValue() < 10 ? "0"+date.getMonthValue(): date.getMonthValue())+ "." + date.getYear();
            String day = stringDate.substring(0, 2);
            String month = stringDate.substring(3, 5);
            String year = stringDate.substring(6, 10);
            String formattedDate = year + month + day;
            String urlGovUa = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?date="
                    + formattedDate + "&amp;json";
            String resultGovUa = httpRequestService.getJSONResult(urlGovUa);

            if (!(resultGovUa.equals(""))) {
                List<CurrencyGovUa> currencyGovUa = jacksonParsingService.parseJSON(resultGovUa);

                for (CurrencyGovUa cur : currencyGovUa
                ) {
                    if (currency.equals(cur.getCc())) {
                        listCurrencyGovUa.add(cur);
                    }
                }
            }
            else  logger.info("No response from bank.gov.ua");
        }

        float maxCurrencyValue = getmaxCurrencyValue(listCurrencyGovUa);

        Stream<CurrencyGovUa> stream = new ArrayList<>(listCurrencyGovUa.subList(0, listCurrencyGovUa.size())).stream();
        stream.filter(cur -> cur.getRate() != maxCurrencyValue).forEach(listCurrencyGovUa:: remove);

        if (listCurrencyGovUa.size() > 0) {
            responseResult.append("The best ").append(currency).append(" exchange rate last ").append(period)
                    .append(": ").append(maxCurrencyValue).append(" (");
            for (int i = 0; i < listCurrencyGovUa.size(); i++) {
                responseResult.append(listCurrencyGovUa.get(i).getExchangedate());
                if (i != listCurrencyGovUa.size() - 1) responseResult.append(" , ");
            }
            responseResult.append(")");
        }
        String returnMessage = responseResult.toString().equals("") ? "Currency info not found!" : responseResult.toString();
        logger.info("Response result:" + returnMessage);
        return returnMessage;
    }

    public float getmaxCurrencyValue(ArrayList<CurrencyGovUa> listCurrencyGovUa) {
        float maxCurrencyValue = 0;
        for (CurrencyGovUa cur: listCurrencyGovUa
        ) {
            if(cur.getRate() > maxCurrencyValue) maxCurrencyValue = cur.getRate();
        }
        logger.info("Max currency value = " + maxCurrencyValue);
        return maxCurrencyValue;
    }
}
