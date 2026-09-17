package com.sds.cleancode.restaurant;

public class TestableSmsSender extends SmsSender{
    private boolean isSendMethodsCalled;

    @Override
    public void send(Schedule schedule){
        System.out.println("테스트용 SmsSender class 의 send메서드 실행됨");
        isSendMethodsCalled = true;
    }

    public boolean isSmsSenderCalled() {
        return isSendMethodsCalled;
    }
}
