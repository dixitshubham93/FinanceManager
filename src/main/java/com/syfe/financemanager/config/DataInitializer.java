package com.syfe.financemanager.config;

import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.enums.TransactionType;
import com.syfe.financemanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    private record CategorySeed(String name, TransactionType type) {}

    private static final List<CategorySeed> DEFAULT_CATEGORIES = List.of(
            new CategorySeed("Salary", TransactionType.INCOME),
            new CategorySeed("Food", TransactionType.EXPENSE),
            new CategorySeed("Rent", TransactionType.EXPENSE),
            new CategorySeed("Transportation", TransactionType.EXPENSE),
            new CategorySeed("Entertainment", TransactionType.EXPENSE),
            new CategorySeed("Healthcare", TransactionType.EXPENSE),
            new CategorySeed("Utilities", TransactionType.EXPENSE)
    );

    @Override
    public void run(ApplicationArguments args) {
        int seeded = 0;
        for (CategorySeed seed : DEFAULT_CATEGORIES) {
            if (!categoryRepository.existsByNameAndUserIsNull(seed.name())) {
                Category category = Category.builder()
                        .name(seed.name())
                        .type(seed.type())
                        .isCustom(false)
                        .user(null)
                        .isDeleted(false)
                        .build();
                categoryRepository.save(category);
                seeded++;
            }
        }
        if (seeded > 0) {
            log.info("DataInitializer: seeded {} default categories", seeded);
        } else {
            log.info("DataInitializer: all default categories already exist, skipping");
        }
    }
}
