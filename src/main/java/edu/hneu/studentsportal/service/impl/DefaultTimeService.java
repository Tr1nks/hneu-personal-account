package edu.hneu.studentsportal.service.impl;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;

import org.springframework.stereotype.Service;

import edu.hneu.studentsportal.service.TimeService;

@Service
public class DefaultTimeService implements TimeService {


    @Override
    public long getCurrentTime() {
        return new Date().getTime();
    }

    @Override
    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    @Override
    public int getCurrentEducationWeek() {
        int firstWeek = getFirstEducationWeek();
        int currentWeek = getCurrentDate().get(getWeekField());
        return currentWeek - firstWeek + 1;

    }

    private int getFirstEducationWeek() {
        int educationYear = getEducationYearForDate(getCurrentDate());
        return LocalDate.of(educationYear, Month.SEPTEMBER, 1).get(getWeekField());
    }

    private int getEducationYearForDate(LocalDate date) {
        if(date.getMonth().compareTo(Month.DECEMBER) >= 0 )
            return date.getYear() - 1;
        else
            return date.getYear();
    }

    private TemporalField getWeekField() {
        Locale locale = Locale.forLanguageTag("ua");
        return WeekFields.of(locale).weekOfWeekBasedYear();
    }

}
