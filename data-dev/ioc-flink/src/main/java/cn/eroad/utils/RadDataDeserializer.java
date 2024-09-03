package cn.eroad.utils;

import com.alibaba.fastjson2.JSON;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;


public class RadDataDeserializer<T> implements DeserializationSchema<T> {
    Class<T> tClass;

    public RadDataDeserializer(Class<T> tClass) {
        this.tClass = tClass;
    }

    // support UTF-8
    @Override
    public T deserialize(byte[] message) {
        return JSON.parseObject(message, tClass);
    }

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return TypeInformation.of(tClass);
    }
}

