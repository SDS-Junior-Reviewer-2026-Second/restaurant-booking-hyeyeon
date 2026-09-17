package com.sds.cleancode.restaurant;

public class TestableMailSender extends MailSender{

    private int countMailSenderCalled = 0;

    @Override
    public void sendMail(Schedule schedule){
        System.out.println("테스트용 MailSender class 의 send 메서드 "+countMailSenderCalled+"번 실행됨");
        countMailSenderCalled++;
    }

    public int getCountMailSenderCalled() {
        return countMailSenderCalled;
    }
}
