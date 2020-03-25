package ua.edu.sumdu.chornobai.lab2spring.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {
    String currency;
    String period;
    String message;
    ArrayList<CurrencyValue> listOfValue;
}
