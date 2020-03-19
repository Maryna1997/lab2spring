package ua.edu.sumdu.сhornobai.lab2spring.services;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.сhornobai.lab2spring.model.CurrencyPrivatbank;

import java.util.ArrayList;

@Service
public class OrgJSONParsingService {
    final static Logger logger = Logger.getLogger(OrgJSONParsingService.class);

    public void parseJSON (String resultJSON, String date, String requestedCurrency, ArrayList<CurrencyPrivatbank> listCurrencyPrivatbank) {
       if (resultJSON == null) {
           return;
       }
        JSONArray jsonArray = new JSONObject(resultJSON).getJSONArray("exchangeRate");
        for (Object currency: jsonArray
        ) {
            JSONObject jsonCurrency = (JSONObject) currency;
            try {
                CurrencyPrivatbank newCurrencyPrivatbank = new CurrencyPrivatbank();
                newCurrencyPrivatbank.setTitle(jsonCurrency.getString("currency"));
                newCurrencyPrivatbank.setDate(date);
                newCurrencyPrivatbank.setSaleRate(jsonCurrency.getFloat("saleRate"));
                newCurrencyPrivatbank.setPurchaseRate(jsonCurrency.getFloat("purchaseRate"));
                if (requestedCurrency.equals(newCurrencyPrivatbank.getTitle())) {
                    listCurrencyPrivatbank.add(newCurrencyPrivatbank);
                    logger.info("Add new element to listCurrencyPrivatbank:" + newCurrencyPrivatbank);
                }
            } catch (JSONException e) {
                logger.log(Level.FATAL, "Exception: ", e);
            }
        }
    }
}
