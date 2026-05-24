package com.syfe.financemanager.dto.request;

import com.syfe.financemanager.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Category type is required (INCOME or EXPENSE)")
    private TransactionType type;
}
