package cn.eroad.util.converter;

import cn.eroad.util.enums.Endian;
import org.springframework.stereotype.Component;

@Component
public class UpperHexConverter implements Converter<String> {

    @Override
    public String convert(byte[] bytes, int start, int length, Endian endian, Object o) {
        StringBuilder result = new StringBuilder();
        if (endian == Endian.BIG) {
            for (int i = 0; i < length; i++) {
                result.append(String.format("%02X", bytes[start + i]));
            }
        } else {
            for (int i = 0; i < length; i++) {
                result.append(String.format("%02X", bytes[start + length - i - 1]));
            }
        }
        return result.toString();
    }
}
