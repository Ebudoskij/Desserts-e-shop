package com.ebudoskij.dessert_shop.utils.specifications;

import com.ebudoskij.dessert_shop.model.AdditionalItem;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemFilterDto;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public class AdditionalItemSpecificationUtil {
    public static Specification<AdditionalItem> buildFilters(AdditionalItemFilterDto filter) {
        Specification<AdditionalItem> spec = Specification.unrestricted();

        if (StringUtils.hasText(filter.getSearchQuery())){
            spec = spec.and(hasKeywordInNameOrDescription(filter.getSearchQuery()));
        }

        if (filter.getDeleted() != null){
            spec = spec.and(isDeleted(filter.getDeleted()));
        }

        if (filter.getMinPrice() != null){
            spec = spec.and(hasPriceGreaterThatOrEqual(filter.getMinPrice()));
        }

        if (filter.getMaxPrice() != null){
            spec = spec.and(hasPriceLessThatOrEqual(filter.getMaxPrice()));
        }

        return spec;
    }

    public static Specification<AdditionalItem> hasKeywordInNameOrDescription(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<AdditionalItem> isDeleted(Boolean isDeleted) {
        return ((root, query, cb) ->
                cb.equal(root.get("isDeleted"), isDeleted));
    }

    public static Specification<AdditionalItem> hasPriceGreaterThatOrEqual(BigDecimal minPrice) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(
                        root.get("extraPrice"),
                        minPrice
                );
    }

    public static Specification<AdditionalItem> hasPriceLessThatOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(
                        root.get("extraPrice"),
                        maxPrice
                );
    }
}
