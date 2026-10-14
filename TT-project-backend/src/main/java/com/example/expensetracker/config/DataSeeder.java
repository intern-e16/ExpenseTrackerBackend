package com.example.expensetracker.config;

import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Inserts a few demo rows on first run so the UI isn't empty. Disabled under the "test" profile. */
@Configuration
@Profile("!test")
public class DataSeeder {

    @Bean
    CommandLineRunner seed(ExpenseRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            LocalDate today = LocalDate.now();
            repository.saveAll(List.of(
                    new Expense("Groceries", new BigDecimal("1250.50"), Category.FOOD, today.minusDays(1), "Weekly shop"),
                    new Expense("Bus pass", new BigDecimal("600.00"), Category.TRANSPORT, today.minusDays(3), null),
                    new Expense("Electricity bill", new BigDecimal("1840.00"), Category.UTILITIES, today.minusDays(5), "September"),
                    new Expense("Movie night", new BigDecimal("450.00"), Category.ENTERTAINMENT, today.minusDays(7), null),
                    new Expense("Textbook", new BigDecimal("899.00"), Category.EDUCATION, today.minusDays(9), "Signals & Systems")
            ));
        };
    }
}
