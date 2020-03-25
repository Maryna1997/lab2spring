package ua.edu.sumdu.chornobai.lab2spring.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyValue {
    String bank;
    String date;
    float purchaseRate;
    float saleRate;
    String message;
}
