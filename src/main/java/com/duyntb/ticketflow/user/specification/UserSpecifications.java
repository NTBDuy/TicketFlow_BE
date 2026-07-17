package com.duyntb.ticketflow.user.specification;

import com.duyntb.ticketflow.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {
    public static Specification<User> hasUser(String keyword) {
        return (((root, query, criteriaBuilder) -> {
            query.distinct(true);

            if (keyword == null || keyword.isBlank()) return null;

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("fullName")),
                            "%" + keyword.toLowerCase() + "%"
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("email")),
                            "%" + keyword.toLowerCase() + "%"
                    )
            );
        }));
    }
}
