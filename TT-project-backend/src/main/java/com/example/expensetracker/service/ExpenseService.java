package com.example.expensetracker.service;

import com.example.expensetracker.dto.ExpenseRequest;
import com.example.expensetracker.dto.ExpenseSummary;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public List<Expense> search(Category category, LocalDate from, LocalDate to) {
        Specification<Expense> spec = (root, query, cb) -> cb.conjunction();
        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), to));
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id"));
        return repository.findAll(spec, sort);
    }

    public Expense get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Transactional
    public Expense create(ExpenseRequest request) {
        Expense expense = new Expense();
        apply(expense, request);
        return repository.save(expense);
    }

    @Transactional
    public Expense update(Long id, ExpenseRequest request) {
        Expense expense = get(id);
        apply(expense, request);
        return repository.save(expense);
    }

    @Transactional
    public void delete(Long id) {
        Expense expense = get(id);
        repository.delete(expense);
    }

    public ExpenseSummary summarize(Category category, LocalDate from, LocalDate to) {
        List<Expense> expenses = search(category, from, to);
        Map<Category, BigDecimal> byCategory = new EnumMap<>(Category.class);
        BigDecimal total = BigDecimal.ZERO;
        for (Expense e : expenses) {
            total = total.add(e.getAmount());
            byCategory.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }
        return new ExpenseSummary(total, expenses.size(), byCategory);
    }

    private void apply(Expense expense, ExpenseRequest r) {
        expense.setTitle(r.title().trim());
        expense.setAmount(r.amount());
        expense.setCategory(r.category());
        expense.setDate(r.date());
        expense.setNotes(r.notes() == null || r.notes().isBlank() ? null : r.notes().trim());
    }
}
