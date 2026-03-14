package com.ebudoskij.dessert_shop.utils.specifications;

import com.ebudoskij.dessert_shop.model.Product;
import com.ebudoskij.dessert_shop.model.dto.product.ProductFilteringDto;
import com.ebudoskij.dessert_shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductSpecificationsUtil {

    private final CategoryService categoryService;

    public Specification<Product> buildFilters(ProductFilteringDto filter) {
        Specification<Product> spec = Specification.unrestricted();

        if (StringUtils.hasText(filter.getSearchQuery())){
            spec = spec.and(hasKeywordInNameOrDescription(filter.getSearchQuery()));
        }

        if (filter.getDeleted() != null){
            spec = spec.and(isDeleted(filter.getDeleted()));
        }

        if (filter.getCategoryId() != null) {

            List<Long> ids = categoryService.getCategoryAndChildrenIds(filter.getCategoryId());

            spec = spec.and(ProductSpecificationsUtil.hasCategoryIds(ids));
        }

        if (filter.getMinPrice() != null){
            spec = spec.and(hasPriceGreaterThatOrEqual(filter.getMinPrice()));
        }

        if (filter.getMaxPrice() != null){
            spec = spec.and(hasPriceLessThatOrEqual(filter.getMaxPrice()));
        }

        return spec;
    }

    public static Specification<Product> hasKeywordInNameOrDescription(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Product> isDeleted(Boolean isDeleted) {
        return (root, query, cb) ->
                cb.equal(root.get("isDeleted"), isDeleted);
    }

    public static Specification<Product> hasPriceGreaterThatOrEqual(BigDecimal minPrice) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(
                        root.get("pricePerUnit"),
                        minPrice
                );
    }

    public static Specification<Product> hasPriceLessThatOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(
                        root.get("pricePerUnit"),
                        maxPrice
                );
    }

    public static Specification<Product> hasCategoryIds(List<Long> categoryIds) {
        return (root, query, cb) ->
                root.get("category").get("id").in(categoryIds);
    }
}
