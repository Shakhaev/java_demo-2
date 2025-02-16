package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.t1.java.demo.model.entity.TransactionAccept;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionAcceptRepository extends JpaRepository<TransactionAccept, Long> {

    @Query("SELECT ta FROM TransactionAccept ta WHERE ta.createdAt BETWEEN :oneHourAgo AND :now AND ta.isBlocked = false")
    List<TransactionAccept> findAllByCreatedAtBetweenAndIsBlockedFalse(@Param("oneHourAgo") LocalDateTime oneHourAgo,
                                                                       @Param("now") LocalDateTime now);

}

