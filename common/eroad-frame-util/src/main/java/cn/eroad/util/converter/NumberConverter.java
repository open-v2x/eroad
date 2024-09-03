package cn.eroad.util.converter;

import cn.eroad.util.enums.Endian;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

@Component
public class NumberConverter implements Converter<Number> {

    @Override
    public Number convert(byte[] bytes, int start, int length, Endian endian, Object o) {
        // 处理成number需要定义对应的数字类型，如果没有，则默认为int
        Class<?> type;
        if (o instanceof Class) {
            type = (Class<?>) o;
        } else {
            type = int.class;
        }
        // 处理不同的数字类型
        if (byte.class.equals(type) || Byte.class.equals(type)) {
            return getByte(bytes, start, length, type);
        } else if (short.class.equals(type) || Short.class.equals(type)) {
            return getShort(bytes, start, length, endian, type);
        } else if (int.class.equals(type) || Integer.class.equals(type)) {
            return getInteger(bytes, start, length, endian, type);
        } else if (long.class.equals(type) || Long.class.equals(type)) {
            return getLong(bytes, start, length, endian, type);
        } else if (float.class.equals(type) || Float.class.equals(type)) {
            return getFloat(bytes, start, length, endian, type);
        } else if (double.class.equals(type) || Double.class.equals(type)) {
            return getDouble(bytes, start, length, endian, type);
        } else if (BigInteger.class.equals(type)) {
            return getBigInteger(bytes, start, length, endian);
        } else if (BigDecimal.class.equals(type)) {
            return getBigDecimal(bytes, start, length, endian);
        }
        return null;
    }

    private Number getByte(byte[] bytes, int start, int length, Class<?> type) {
        return length == 1 ? bytes[start] : byte.class.equals(type) ? (byte) 0 : null;
    }

    private Number getShort(byte[] bytes, int start, int length, Endian endian, Class<?> type) {
        switch (length) {
            case 1:
                return (short) bytes[start];
            case 2:
                return (short) (endian == Endian.BIG ? ((bytes[start] & 0xff) << 8 | (bytes[start + 1] & 0xff))
                        : ((bytes[start + 1] & 0xff) << 8 | (bytes[start] & 0xff)));
            default:
                return short.class.equals(type) ? (short) 0 : null;
        }
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
            result = result << 8 | bytes[index] & 0xff;
            index += step;
        }
        return result;
    }

    private Number getLong(byte[] bytes, int start, int length, Endian endian, Class<?> type) {
        if (length < 1 || length > 8) {
            return long.class.equals(type) ? 0L : null;
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
        long result = 0;
        for (int i = 0; i < length; i++) {
            result = result << 8 | bytes[index] & 0xff;
            index += step;
        }
        return result;
    }

    private Number getFloat(byte[] bytes, int start, int length, Endian endian, Class<?> type) {
        if (length != 4) {
            return float.class.equals(type) ? (float) 0 : null;
        }
        int result = (int) getInteger(bytes, start, length, endian, int.class);
        return Float.intBitsToFloat(result);
    }

    private Number getDouble(byte[] bytes, int start, int length, Endian endian, Class<?> type) {
        if (length != 8) {
            return double.class.equals(type) ? (double) 0 : null;
        }
        long result = (long) getLong(bytes, start, length, endian, long.class);
        return Double.longBitsToDouble(result);
    }

    private Number getBigInteger(byte[] bytes, int start, int length, Endian endian) {
        byte[] value = Arrays.copyOfRange(bytes, start, start + length);
        if (endian == Endian.LITTLE) {
            reverse(value);
        }
        return new BigInteger(value);
    }

    private void reverse(byte[] bytes) {
        for (int i = 0; i < bytes.length / 2 - 1; i++) {
            byte temp = bytes[i];
            int index = bytes.length - i - 1;
            bytes[i] = bytes[index];
            bytes[index] = temp;
        }
    }

    private Number getBigDecimal(byte[] bytes, int start, int length, Endian endian) {
        Double value = (Double) getDouble(bytes, start, length, endian, Double.class);
        return value == null ? null : new BigDecimal(value);
    }
}
