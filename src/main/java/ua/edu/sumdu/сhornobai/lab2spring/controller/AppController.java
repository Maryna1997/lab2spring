package ua.edu.sumdu.сhornobai.lab2spring.controller;

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

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


@RestController
@RequestMapping(path = "/apicurrency")

public class AppController {

    public HTTPRequestService httpRequestService;
    public OrgJSONParsingService orgJSONParsingService;
    public JacksonParsingService jacksonParsingService;
    public RestTemplatesParsingService restTemplatesParsingService;
    private Object getStream;

    public AppController(HTTPRequestService httpRequestService, OrgJSONParsingService orgJSONParsingService, JacksonParsingService jacksonParsingService, RestTemplatesParsingService restTemplatesParsingService) {
        this.httpRequestService = httpRequestService;
        this.orgJSONParsingService = orgJSONParsingService;
        this.jacksonParsingService = jacksonParsingService;
        this.restTemplatesParsingService = restTemplatesParsingService;
    }

    public Stream<ArrayList<CurrencyGovUa>> getStream(ArrayList<CurrencyGovUa> listCurrencyGovUa) {
        return new ArrayList<>(Arrays.asList(listCurrencyGovUa).subList(0, listCurrencyGovUa.size())).stream();
    }

    @RequestMapping(path = "/exchangeRate/{currency}/{date}", method = RequestMethod.GET)
    public String getExchangeRate(@PathVariable(name = "currency") String currency, @PathVariable(name = "date") String date) throws IOException {
        String day = date.substring(0, 2);
        String month = date.substring(3, 5);
        String year = date.substring(6, 10);
        String formattedDate = year +month +day;
        int dd = Integer.parseInt(day);
        int mm = Integer.parseInt(month);
        int yyyy = Integer.parseInt(year);
        LocalDate localDate = LocalDate.of(yyyy, mm, dd);

        int currencyCode = 0;

        if(localDate.isBefore(LocalDate.now()) || localDate.equals(LocalDate.now())) {
            String responseResult = "";
            String urlPrivatbank = "https://api.privatbank.ua/p24api/exchange_rates?json&date=" + date;
            String resultPrivatbank = httpRequestService.getJSONResult(urlPrivatbank);
            ArrayList<CurrencyPrivatbank> listCurrencyPrivatbank = new ArrayList<>();
            orgJSONParsingService.parseJSON(resultPrivatbank, date, currency, listCurrencyPrivatbank);

            for (CurrencyPrivatbank cur : listCurrencyPrivatbank
            ) {
                System.out.println(cur);
//                if (currency.equals(cur.getTitle())) {
                   responseResult = responseResult + "Privatbank:" + cur.getTitle() + System.lineSeparator() + " - PurchaseRate: " + cur.getPurchaseRate() + " - SaleRate: " + cur.getSaleRate();
//                }
            }

            //jackson


            String urlGovUa = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?date=" + formattedDate + "&amp;json";
            String resultGovUa = httpRequestService.getJSONResult(urlGovUa);

            List<CurrencyGovUa> currencyGovUa = jacksonParsingService.parseJSON(resultGovUa);

            for (CurrencyGovUa cur : currencyGovUa
            ) {
                System.out.println(cur);
                if (currency.equals(cur.getCc())) {
                    responseResult = responseResult + " --------" + " GovUa:" + cur.getCc() + " - Rate: " + cur.getRate();
                    currencyCode = Integer.parseInt(cur.getR030());
                }
            }

            //RestTemplates
            if(localDate.equals(LocalDate.now())) {
                String resultMonobank = "";
                CurrencyMonobank[] currencyMonobank = restTemplatesParsingService.parseJSON();

                for (CurrencyMonobank cur : currencyMonobank
                ) {
                    System.out.println(cur);

                    if (currencyCode == cur.getCurrencyCodeA() && cur.getCurrencyCodeB() == 980) {
                        responseResult = responseResult + " --------" + " Monobank:"  + " - PurchaseRate: " + cur.getRateBuy() + " - SaleRate: " + cur.getRateSell();
                    }
                }
            }

            return (responseResult ==  "" ? "Currency info not found!": responseResult) ;
        }
        else return "Please enter a valid date";
    }

    @RequestMapping(path = "/bestExchangeRate/{currency}/{period}", method = RequestMethod.GET)
    public String getBestExchangeRate(@PathVariable(name = "currency") String currency, @PathVariable(name = "period") String period) throws IOException {
    switch (period){
        case "year":
            return getBestExchangeRateForPeriod(LocalDate.now().plusYears(-1), currency,period);
        case "month":
            return getBestExchangeRateForPeriod(LocalDate.now().plusMonths(-1), currency,period);
        case "week":
            return getBestExchangeRateForPeriod(LocalDate.now().plusDays(-7), currency,period);
        default:
            return "Incorrect period. Please, enter 'week', 'month' or 'year'";
        }
    }

    public String getBestExchangeRateForPeriod(LocalDate startDay, String currency, String period) throws IOException {
        String responseResult = "";
        ArrayList<CurrencyGovUa> listCurrencyGovUa = new ArrayList<>();

        for (LocalDate date = startDay; date.isBefore(LocalDate.now()); date = date.plusDays(1)) {
            String stringDate = (date.getDayOfMonth() <10? "0" + date.getDayOfMonth() : date.getDayOfMonth() ) + "." + (date.getMonthValue() < 10 ? "0"+date.getMonthValue(): date.getMonthValue())+ "." + date.getYear();
            String day = stringDate.substring(0, 2);
            String month = stringDate.substring(3, 5);
            String year = stringDate.substring(6, 10);
            String formattedDate = year +month +day;
            String urlGovUa = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?date=" + formattedDate + "&amp;json";
            String resultGovUa = httpRequestService.getJSONResult(urlGovUa);

            List<CurrencyGovUa> currencyGovUa = jacksonParsingService.parseJSON(resultGovUa);

            for (CurrencyGovUa cur : currencyGovUa
            ) {
                if (currency.equals(cur.getCc())) {
                    listCurrencyGovUa.add(cur);
                }
            }

        }
        float maxCurrencyValue = getmaxCurrencyValue(listCurrencyGovUa);

        Stream<CurrencyGovUa> stream = new ArrayList<>(listCurrencyGovUa.subList(0, listCurrencyGovUa.size())).stream();

        stream.filter(cur -> cur.getRate() != maxCurrencyValue).forEach(listCurrencyGovUa:: remove);
        if (listCurrencyGovUa.size() > 0) {
            responseResult += "The best " + currency + " exchange rate last " + period + ": " + maxCurrencyValue + " (";
            for (int i = 0; i < listCurrencyGovUa.size(); i++) {
                responseResult += listCurrencyGovUa.get(i).getExchangedate();
                if (i != listCurrencyGovUa.size() - 1) responseResult += " , ";
            }
            responseResult += ")";
        }
        return responseResult ==  "" ? "Currency info not found!":responseResult;
    }

    public float getmaxCurrencyValue(ArrayList<CurrencyGovUa> listCurrencyGovUa) {
        float maxCurrencyValue = 0;
        for (CurrencyGovUa cur: listCurrencyGovUa
        ) {
            if(cur.getRate() > maxCurrencyValue) maxCurrencyValue = cur.getRate();
        }
        return maxCurrencyValue;
    }
}
