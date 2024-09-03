package cn.eroad.util.decorator;

public interface Decorator<I, O> {

    O decorate(I i, double magnitude, String prefix, String suffix);
}
