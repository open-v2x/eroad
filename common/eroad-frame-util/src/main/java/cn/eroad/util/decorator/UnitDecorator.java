package cn.eroad.util.decorator;

import org.springframework.stereotype.Component;

@Component
public class UnitDecorator extends PrefixSuffixDecorator {

    @Override
    public String decorate(Object o, double magnitude, String prefix, String suffix) {
        return super.decorate(o, magnitude, null, suffix);
    }
}
