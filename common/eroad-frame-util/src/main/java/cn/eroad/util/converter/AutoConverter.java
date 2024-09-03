package cn.eroad.util.converter;

import cn.eroad.util.enums.Endian;
import org.springframework.stereotype.Component;

@Component
public class AutoConverter implements Converter<Object> {

    @Override
    public Object convert(byte[] bytes, int start, int length, Endian endian, Object o) {
        return null;
    }
}
