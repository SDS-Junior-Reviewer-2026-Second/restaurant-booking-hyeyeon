package com.sds.cleancode.restaurant;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Fail.fail;

@ExtendWith(MockitoExtension.class)
public class BookingSchedulerTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private static final LocalDateTime NOT_ON_THE_HOUR = LocalDateTime.parse("2021/03/26 09:04", FORMATTER);
    private static final LocalDateTime ON_THE_HOUR = LocalDateTime.parse("2021/03/26 09:00", FORMATTER);
    @Mock
    private Customer CUSTOMER;

    @Mock(answer= Answers.RETURNS_MOCKS)
    private Customer CUSTOMER_WITH_MAIL;

    private static final int UNDER_CAPACITY = 1;
    private static final int CAPACITY_PER_HOUR = 3;

    private static final String CAPACITY_OVER_ERROR = "Number of people is over restaurant capacity per hour";
    private static final String BOOKING_SYSTEM_NOT_AVAILABLE = "Booking system is not available on sunday";

    private static final String SUNDAY = "2026/09/13 17:00";
    private static final String MONDAY = "2026/09/14 17:00";

    @Spy
    public BookingScheduler bookingScheduler;

    @Mock
    public SmsSender smsSender;
    @Mock
    public MailSender mailSender;

    @BeforeEach
    void setUp() {
        bookingScheduler.setSmsSender(smsSender);
        bookingScheduler.setMailSender(mailSender);
    }

    public BookingSchedulerTest(){
        bookingScheduler = new BookingScheduler(CAPACITY_PER_HOUR);
    }


    @Test
    public void 예약은_정시에만_가능하다_정시가_아닌경우_예약불가() {
        Schedule schedule = new Schedule(NOT_ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        assertThatThrownBy(() -> {
            bookingScheduler.addSchedule(schedule);
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void 예약은_정시에만_가능하다_정시인_경우_예약가능() {
        Schedule schedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        bookingScheduler.addSchedule(schedule);

        assertThat(bookingScheduler.hasSchedule(schedule)).isEqualTo(true);
    }

    @Test
    public void 시간대별_인원제한이_있다_같은_시간대에_Capacity_초과할_경우_예외발생() {
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);

        bookingScheduler.addSchedule(schedule);

        try {
            Schedule newSchedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);
            bookingScheduler.addSchedule(newSchedule);
            fail();
        } catch (RuntimeException e){
            assertThat(e.getMessage()).isEqualTo(CAPACITY_OVER_ERROR);
        }
//        assertThatThrownBy(() -> {
//            bookingScheduler.addSchedule(schedule);
//        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void 시간대별_인원제한이_있다_같은_시간대가_다르면_Capacity_차있어도_스케쥴_추가_성공() {
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);
        bookingScheduler.addSchedule(schedule);


        LocalDateTime differentHour = ON_THE_HOUR.plusHours(1);
        Schedule differentSchedule = new Schedule(differentHour, CAPACITY_PER_HOUR, CUSTOMER);
        bookingScheduler.addSchedule(differentSchedule);

        assertThat(bookingScheduler.hasSchedule(schedule)).isEqualTo(true);
    }

    @Test
    public void 예약완료시_SMS는_무조건_발송() {
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);
        bookingScheduler.addSchedule(schedule);

        //assertThat(testableSmsSender.isSmsSenderCalled()).isEqualTo(true);
        verify(smsSender, times(1)).send(schedule);
    }

    @Test
    public void 이메일이_없는_경우에는_이메일_미발송() {
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);
        bookingScheduler.addSchedule(schedule);

        //assertThat(testableMailSender.getCountMailSenderCalled()).isEqualTo(0);
        verify(mailSender, times(0)).sendMail(schedule);
    }

    @Test
    public void 이메일이_있는_경우에는_이메일_발송() {
        Schedule schedule = new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER_WITH_MAIL);
        bookingScheduler.addSchedule(schedule);

        //assertThat(testableMailSender.getCountMailSenderCalled()).isEqualTo(1);
        verify(mailSender, times(1)).sendMail(schedule);

    }

    @Test
    public void 현재날짜가_일요일인_경우_예약불가_예외처리() {
        LocalDateTime sunday = LocalDateTime.parse(SUNDAY, FORMATTER);
        when(bookingScheduler.getNow()).thenReturn(sunday);

        //bookingScheduler = new TestableScheduler(CAPACITY_PER_HOUR, SUNDAY);

        try{
            Schedule schedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER );
            bookingScheduler.addSchedule(schedule);
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo(BOOKING_SYSTEM_NOT_AVAILABLE);
        }
    }

    @Test
    public void 현재날짜가_일요일이_아닌경우_예약가능() {
        LocalDateTime monday = LocalDateTime.parse(MONDAY, FORMATTER);
        when(bookingScheduler.getNow()).thenReturn(monday);
        //bookingScheduler = new TestableScheduler(CAPACITY_PER_HOUR, MONDAY);

        Schedule schedule = new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER );
        bookingScheduler.addSchedule(schedule);

        assertThat(bookingScheduler.hasSchedule(schedule)).isEqualTo(true);
    }
}
