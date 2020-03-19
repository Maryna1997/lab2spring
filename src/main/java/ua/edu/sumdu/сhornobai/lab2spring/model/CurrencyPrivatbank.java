package ua.edu.sumdu.сhornobai.lab2spring.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CurrencyPrivatbank {
    String title;
    String date;
    float saleRate;
    float purchaseRate;
}
