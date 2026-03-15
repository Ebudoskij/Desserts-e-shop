package com.ebudoskij.dessert_shop.repository.custom.additionalItem.impl;

import com.ebudoskij.dessert_shop.model.AdditionalItem;
import com.ebudoskij.dessert_shop.model.Media;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemCardDto;
import com.ebudoskij.dessert_shop.repository.custom.additionalItem.AdditionalItemRepositoryCustom;
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
public class AdditionalItemRepositoryImpl implements AdditionalItemRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<AdditionalItemCardDto> findAdditionalItemCards(Specification<AdditionalItem> spec, Pageable pageable) {
        // Initialize locally to ensure thread-safety
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // 1. Main Query
        CriteriaQuery<AdditionalItemCardDto> query = cb.createQuery(AdditionalItemCardDto.class);
        Root<AdditionalItem> a = query.from(AdditionalItem.class);

        // 2. Subquery for Media URL
        Subquery<String> mediaSubquery = query.subquery(String.class);
        Root<Media> m = mediaSubquery.from(Media.class);

        mediaSubquery.select(m.get("url"))
                .where(cb.and(
                        cb.equal(m.get("entityId"), a.get("id")),
                        cb.equal(m.get("entityType"), "AdditionalItem"),
                        cb.equal(m.get("priority"), createMinPrioritySubquery(cb, query, a.get("id")))
                ));

        // 3. Construct DTO
        query.select(cb.construct(AdditionalItemCardDto.class,
                a.get("id"), a.get("name"), a.get("extraPrice"), a.get("isDeleted"), mediaSubquery));

        // 4. Apply Specification Predicates (The Fix)
        if (spec != null) {
            Predicate predicate = spec.toPredicate(a, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        // 5. Apply Sorting
        if (pageable.getSort().isSorted()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), a, cb));
        }

        // 6. Execute Paged Query
        List<AdditionalItemCardDto> results = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(results, pageable, getTotalCount(spec));
    }

    private long getTotalCount(Specification<AdditionalItem> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<AdditionalItem> root = countQuery.from(AdditionalItem.class);

        countQuery.select(cb.count(root));

        // Ensure null predicates aren't passed to .where()
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, countQuery, cb);
            if (predicate != null) {
                countQuery.where(predicate);
            }
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    // Pass CriteriaBuilder as a parameter to maintain thread-safety
    private Subquery<Integer> createMinPrioritySubquery(CriteriaBuilder cb, CriteriaQuery<?> query, Expression<Long> id) {
        Subquery<Integer> minSub = query.subquery(Integer.class);
        Root<Media> m2 = minSub.from(Media.class);

        return minSub.select(cb.min(m2.get("priority")))
                .where(cb.and(
                        cb.equal(m2.get("entityId"), id),
                        cb.equal(m2.get("entityType"), "AdditionalItem")
                ));
    }
}