package net.bleujin.fn;

import java.util.function.Function;

public class Fn {

	public static <T, R> Function<T, R> wrap(ExceptionFunction<T, R> f) {
		return (T r) -> {
			try {
				return f.apply(r);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}
}
