package net.bleujin.fn;

public interface ExceptionFunction<T, R> {
    R apply(T r) throws Exception;
}