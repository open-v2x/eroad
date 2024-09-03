package cn.eroad.util.converter;

import cn.eroad.util.enums.Endian;
import org.springframework.stereotype.Component;

@Component
public class BooleanConverter implements Converter<Boolean> {

    @Override
    public Boolean convert(byte[] bytes, int start, int length, Endian endian, Object o) {
        Class<?> type;
        if (o instanceof Class) {
            type = (Class<?>) o;
        } else {
            type = boolean.class;
        }
        if (length != 1) {
            return type == boolean.class ? false : null;
        }
        return bytes[start] != 0;
    }
}
