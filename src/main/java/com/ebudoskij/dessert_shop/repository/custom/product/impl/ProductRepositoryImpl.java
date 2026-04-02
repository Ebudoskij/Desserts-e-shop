package com.ebudoskij.dessert_shop.repository.custom.product.impl;

import com.ebudoskij.dessert_shop.model.Media;
import com.ebudoskij.dessert_shop.model.Product;
import com.ebudoskij.dessert_shop.model.dto.product.ProductCardDto;
import com.ebudoskij.dessert_shop.repository.custom.product.ProductRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<ProductCardDto> findProductCards(Specification<Product> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductCardDto> query = cb.createQuery(ProductCardDto.class);
        Root<Product> p = query.from(Product.class);

        // 1. Subquery for media (remains the same)
        Subquery<String> mediaSubquery = query.subquery(String.class);
        Root<Media> m = mediaSubquery.from(Media.class);
        mediaSubquery.select(m.get("url"))
                .where(cb.and(
                        cb.equal(m.get("entityId"), p.get("id")),
                        cb.equal(m.get("entityType"), "Product"),
                        cb.equal(m.get("priority"), createMinPrioritySubquery(query, p.get("id")))
                ));

        // 2. Robust Specification Application
        if (spec != null) {
            // Pass the actual Root (p) and Query (query) to the spec
            Predicate predicate = spec.toPredicate(p, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        // 3. Select DTO
        query.select(cb.construct(ProductCardDto.class,
                p.get("id"),
                p.get("category"),
                p.get("name"),
                p.get("pricePerUnit"),
                p.get("unitType"),
                p.get("customizable"),
                p.get("isDeleted"),
                mediaSubquery
        ));

        // 4. Sorting + Pagination (remains the same)
        if (pageable.getSort().isSorted()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), p, cb));
        }

        List<ProductCardDto> results = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        long total = getTotalCount(spec);

        return new PageImpl<>(results, pageable, total);
    }

    private long getTotalCount(Specification<Product> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder(); // Ensure cb is initialized
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> root = countQuery.from(Product.class);

        countQuery.select(cb.count(root));

        if (spec != null) {
            // Build the predicate
            Predicate predicate = spec.toPredicate(root, countQuery, cb);
            // Hibernate 6+ requirement: Only apply if not null
            if (predicate != null) {
                countQuery.where(predicate);
            }
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Subquery<Integer> createMinPrioritySubquery(CriteriaQuery<?> query, Expression<Long> productId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Subquery<Integer> minSub = query.subquery(Integer.class);
        Root<Media> m2 = minSub.from(Media.class);

        return minSub.select(cb.min(m2.get("priority")))
                .where(cb.and(
                        cb.equal(m2.get("entityId"), productId),
                        cb.equal(m2.get("entityType"), "Product")
                ));
    }
}
