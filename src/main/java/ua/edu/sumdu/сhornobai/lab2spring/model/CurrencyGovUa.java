package ua.edu.sumdu.сhornobai.lab2spring.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CurrencyGovUa {
    String r030;
    String txt;
    float rate;
    String cc;
    String exchangedate;

    public float getRate() {
        return rate;
    }
}
