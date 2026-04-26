package com.validation.strategy;

import java.lang.reflect.Field;
import java.util.Optional;
import com.validation.annotation.NrIndeksu;

public class NrIndeksuStrategy implements ValidationStrategy {
    @Override
    public Optional<String> validate(Field field, Object value) {
        if (field.isAnnotationPresent(NrIndeksu.class) && value instanceof String) {
            String str = (String) value;
            if (!str.matches("\\d{8}")) { // Sprawdza czy jest dokładnie 8 cyfr
                NrIndeksu annotation = field.getAnnotation(NrIndeksu.class);
                return Optional.of(String.format("Pole %s: %s", field.getName(), annotation.message()));
            }
        }
        return Optional.empty();
    }
}