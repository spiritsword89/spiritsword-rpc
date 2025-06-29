package com.spiritsword.model;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class MessageTypeSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer jsonSerializer, Object o, Object o1, Type type, int i) throws IOException {
        if(o == null) {
            jsonSerializer.writeNull();
            return;
        }

        if(o instanceof MessageType) {
            MessageType requestType = (MessageType)o;
            jsonSerializer.write(requestType.name());
        }
    }
}
