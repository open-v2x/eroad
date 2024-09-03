package cn.eroad.util.decorator;

import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class IpDecorator implements Decorator<String, String> {

    @Override
    public String decorate(String s, double magnitude, String prefix, String suffix) {
        if (s == null) {
            return null;
        }
        StringJoiner result = null;
        if (s.length() == 8) {
            result = new StringJoiner(".");
            for (int i = 0; i < 4; i++) {
                result.add(String.valueOf(Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16)));
            }
        } else if (s.length() == 32) {
            result = new StringJoiner(":");
            for (int i = 0; i < 8; i++) {
                result.add(s.substring(i * 4, i * 4 + 4));
            }
        }
        return result == null ? null : result.toString();
    }
}
