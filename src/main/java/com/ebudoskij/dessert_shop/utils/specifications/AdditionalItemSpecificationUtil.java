package com.ebudoskij.dessert_shop.utils.specifications;

import com.ebudoskij.dessert_shop.model.AdditionalItem;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class AdditionalItemSpecificationUtil {
    public static Specification<AdditionalItem> buildFilters(String searchQuery, Boolean deleted) {
        Specification<AdditionalItem> spec = Specification.unrestricted();

        if (StringUtils.hasText(searchQuery)){
            spec = spec.and(hasKeywordInNameOrDescription(searchQuery));
        }

        if (deleted != null){
            spec = spec.and(isDeleted(deleted));
        }

        return spec;
    }

    public static Specification<AdditionalItem> hasKeywordInNameOrDescription(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<AdditionalItem> isDeleted(Boolean isDeleted) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isDeleted"), isDeleted));
    }
}
