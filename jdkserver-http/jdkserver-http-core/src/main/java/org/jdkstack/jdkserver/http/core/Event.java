package org.jdkstack.jdkserver.http.core;

class Event {

    ExchangeImpl exchange;

    protected Event (ExchangeImpl t) {
        this.exchange = t;
    }
}
