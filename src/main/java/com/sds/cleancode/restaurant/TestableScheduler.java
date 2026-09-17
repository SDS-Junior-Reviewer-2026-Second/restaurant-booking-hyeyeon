package com.sds.cleancode.restaurant;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestableScheduler extends BookingScheduler{
    private String dateTime;

    public TestableScheduler(int capacityPerHour, String dateTime){
        super(capacityPerHour);
        this.dateTime = dateTime;
    }

    @Override
    public LocalDateTime getNow(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        return LocalDateTime.parse(dateTime, formatter);
    }

}
