package cn.eroad.util.converter;

import cn.eroad.util.enums.Endian;
import org.springframework.stereotype.Component;

@Component
public class CharConverter implements Converter<Character> {

    @Override
    public Character convert(byte[] bytes, int start, int length, Endian endian, Object o) {
        Class<?> type;
        if (o instanceof Class) {
            type = (Class<?>) o;
        } else {
            type = char.class;
        }
        if (length < 1 || length > 2) {
            return type == char.class ? (char) 0 : null;
        }
        return (char) (int) getInteger(bytes, start, length, endian, int.class);
    }

    private Number getInteger(byte[] bytes, int start, int length, Endian endian, Class<?> type) {
        if (length < 1 || length > 4) {
            return int.class.equals(type) ? 0 : null;
        }
        int index;
        int step;
        if (endian == Endian.BIG) {
            index = start;
            step = 1;
        } else {
            index = start + length - 1;
            step = -1;
        }
        int result = 0;
        for (int i = 0; i < length; i++) {
            result = result << 8 | bytes[index];
            index += step;
        }
        return result;
    }
}
