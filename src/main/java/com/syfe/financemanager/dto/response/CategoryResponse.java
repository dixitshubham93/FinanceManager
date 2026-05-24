package com.syfe.financemanager.dto.response;

import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {

    private String name;
    private TransactionType type;
    private boolean isCustom;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .name(category.getName())
                .type(category.getType())
                .isCustom(category.isCustom())
                .build();
    }
}
