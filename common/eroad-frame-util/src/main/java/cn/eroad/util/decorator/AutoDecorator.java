package cn.eroad.util.decorator;

import org.springframework.stereotype.Component;

@Component
public class AutoDecorator implements Decorator<Object, Object> {

    @Override
    public Object decorate(Object o, double magnitude, String prefix, String suffix) {
        return o;
    }
}
