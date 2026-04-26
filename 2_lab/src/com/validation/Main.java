package com.validation;

import com.validation.exception.ValidationException;
import com.validation.validator.Validator;

public class Main {
    public static void main(String[] args) {
        //student z błędnymi danymi
        Student student = new Student("Wi", "Do", "124", "wi");

        try {
            Validator.validate(student);
            System.out.println("Dane poprawne!");
        } catch (ValidationException e) {
            System.err.println("Znaleziono błędy walidacji:\n" + e.getMessage());
        }
    }
}