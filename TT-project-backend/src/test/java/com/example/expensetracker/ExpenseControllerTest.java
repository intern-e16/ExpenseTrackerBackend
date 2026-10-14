package com.example.expensetracker;

import com.example.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ExpenseRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private static String json(String title, String amount, String category, String date) {
        return """
                {"title":"%s","amount":%s,"category":"%s","date":"%s","notes":"n"}
                """.formatted(title, amount, category, date);
    }

    private void post(String body) throws Exception {
        mvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void createsAndReadsExpense() throws Exception {
        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Lunch", "12.50", "FOOD", "2026-10-01")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title", is("Lunch")))
                .andExpect(jsonPath("$.date", is("2026-10-01")));

        mvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "-5", "FOOD", "2026-10-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    void rejectsUnknownCategory() throws Exception {
        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("X", "5", "NOPE", "2026-10-01")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filtersByCategoryAndSummarizes() throws Exception {
        post(json("Lunch", "10.00", "FOOD", "2026-10-01"));
        post(json("Dinner", "20.00", "FOOD", "2026-10-02"));
        post(json("Bus", "5.00", "TRANSPORT", "2026-10-03"));

        mvc.perform(get("/api/expenses").param("category", "FOOD"))
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/api/expenses").param("from", "2026-10-02").param("to", "2026-10-03"))
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(35.0)))
                .andExpect(jsonPath("$.count", is(3)))
                .andExpect(jsonPath("$.byCategory.FOOD", is(30.0)))
                .andExpect(jsonPath("$.byCategory.TRANSPORT", is(5.0)));
    }

    @Test
    void updatesAndDeletes() throws Exception {
        post(json("Old", "1.00", "OTHER", "2026-10-01"));
        long id = repository.findAll().get(0).getId();

        mvc.perform(put("/api/expenses/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("New", "2.00", "HEALTH", "2026-10-05")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New")))
                .andExpect(jsonPath("$.category", is("HEALTH")));

        mvc.perform(delete("/api/expenses/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/expenses/" + id)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/expenses/" + id)).andExpect(status().isNotFound());
    }
}
