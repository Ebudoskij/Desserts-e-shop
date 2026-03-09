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

    private CriteriaBuilder cb; // Declared at class level for helper access

    @Override
    public Page<AdditionalItemCardDto> findAdditionalItemCards(Specification<AdditionalItem> spec, Pageable pageable) {
        this.cb = entityManager.getCriteriaBuilder();

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
                        cb.equal(m.get("priority"), createMinPrioritySubquery(query, a.get("id")))
                ));

        // 3. Construct DTO
        query.select(cb.construct(AdditionalItemCardDto.class,
                a.get("id"), a.get("name"), a.get("extraPrice"), mediaSubquery));

        // 4. Apply Specification Predicates
        if (spec != null) {
            query.where(spec.toPredicate(a, query, cb));
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

    // Helper: Gets the total count for the Page object
    private long getTotalCount(Specification<AdditionalItem> spec) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<AdditionalItem> root = countQuery.from(AdditionalItem.class);

        countQuery.select(cb.count(root));
        if (spec != null) {
            countQuery.where(spec.toPredicate(root, countQuery, cb));
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    // Helper: The subquery for MIN(priority)
    private Subquery<Integer> createMinPrioritySubquery(CriteriaQuery<?> query, Expression<Long> id) {
        Subquery<Integer> minSub = query.subquery(Integer.class);
        Root<Media> m2 = minSub.from(Media.class);

        return minSub.select(cb.min(m2.get("priority")))
                .where(cb.and(
                        cb.equal(m2.get("entityId"), id),
                        cb.equal(m2.get("entityType"), "AdditionalItem")
                ));
    }
}
