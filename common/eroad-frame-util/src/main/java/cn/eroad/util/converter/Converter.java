package cn.eroad.util.converter;

import cn.eroad.util.enums.Endian;

public interface Converter<T> {

    T convert(byte[] bytes, int start, int length, Endian endian, Object o);
}
