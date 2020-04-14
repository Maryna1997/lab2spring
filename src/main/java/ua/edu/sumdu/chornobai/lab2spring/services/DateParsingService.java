package ua.edu.sumdu.chornobai.lab2spring.services;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DateParsingService {
    public String getFormattedDate(String date){
        String day = date.substring(0, 2);
        String month = date.substring(3, 5);
        String year = date.substring(6, 10);
        return year + month + day;
    }

    public String getStringDate(LocalDate currentDate){
        return (currentDate.getDayOfMonth() <10? "0" + currentDate.getDayOfMonth() : currentDate.getDayOfMonth() )
                + "." + (currentDate.getMonthValue() < 10 ? "0" + currentDate.getMonthValue(): currentDate.getMonthValue())
                + "." + currentDate.getYear();
    }
}
