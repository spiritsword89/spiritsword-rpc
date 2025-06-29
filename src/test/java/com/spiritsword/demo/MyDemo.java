package com.spiritsword.demo;

import com.spiritsword.model.MessagePayload;
import org.junit.jupiter.api.Test;

public class MyDemo {

    @Test
    public void run() {
        Class<MessagePayload> messagePayloadClass = MessagePayload.class;
        String name = messagePayloadClass.getName();
        System.out.println(name);
    }
}
