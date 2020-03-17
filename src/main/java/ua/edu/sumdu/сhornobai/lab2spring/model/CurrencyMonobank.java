package ua.edu.sumdu.сhornobai.lab2spring.model;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CurrencyMonobank {
    int currencyCodeA;
    int currencyCodeB;
    int date;
    float rateSell;
    float rateBuy;
    float rateCross;
}
