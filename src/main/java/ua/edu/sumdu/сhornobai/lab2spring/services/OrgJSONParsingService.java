package ua.edu.sumdu.сhornobai.lab2spring.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.сhornobai.lab2spring.model.CurrencyPrivatbank;

import java.util.ArrayList;

@Service
public class OrgJSONParsingService {
    public void parseJSON (String resultJSON, String date, String requestedCurrency, ArrayList<CurrencyPrivatbank> listCurrencyPrivatbank) {
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
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
