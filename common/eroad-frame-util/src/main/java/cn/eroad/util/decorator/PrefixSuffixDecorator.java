package cn.eroad.util.decorator;

import org.springframework.stereotype.Component;

@Component
public class PrefixSuffixDecorator implements Decorator<Object, String> {

    @Override
    public String decorate(Object o, double magnitude, String prefix, String suffix) {
        if (o == null) {
            return null;
        }
        return (prefix == null ? "" : prefix) + o + (suffix == null ? "" : suffix);
    }
}
