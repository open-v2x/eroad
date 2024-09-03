package cn.eroad.util.converter;

import cn.eroad.util.enums.Endian;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class StringConverter implements Converter<String> {

    @Override
    public String convert(byte[] bytes, int start, int length, Endian endian, Object o) {
        if (length % 2 != 0) {
            return null;
        }
        return new String(bytes, start, length, endian == Endian.BIG ? StandardCharsets.UTF_16BE : StandardCharsets.UTF_16LE);
    }
}
