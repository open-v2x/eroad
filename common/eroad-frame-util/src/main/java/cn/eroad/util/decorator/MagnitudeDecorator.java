package cn.eroad.util.decorator;

import org.springframework.stereotype.Component;

@Component
public class MagnitudeDecorator implements Decorator<Number, Double> {

    @Override
    public Double decorate(Number number, double magnitude, String prefix, String suffix) {
        if (number == null) {
            return null;
        }
        if (magnitude == 1) {
            return number.doubleValue();
        }
        return number.doubleValue() * magnitude;
    }
}
