package com.validation.strategy;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import com.validation.annotation.*; // Importuje wszystkie adnotacje z pakietu

public class ValidationStrategyFactory {

    // Mapa przechowująca powiązania między typem adnotacji a klasą strategii
    private static final Map<Class<? extends Annotation>, ValidationStrategy> strategies = new HashMap<>();

    static {
        // Rejestracja strategii dla każdej adnotacji
        strategies.put(NotNull.class, new NotNullStrategy());
        strategies.put(NotEmpty.class, new NotEmptyStrategy());
        strategies.put(Size.class, new SizeStrategy());
        strategies.put(Email.class, new EmailStrategy());
        strategies.put(NrIndeksu.class, new NrIndeksuStrategy());
    }

    private ValidationStrategyFactory() {}

    public static ValidationStrategy getStrategy(Annotation annotation) {
        return strategies.get(annotation.annotationType());
    }
}
