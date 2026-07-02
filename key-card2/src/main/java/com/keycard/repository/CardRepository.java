package com.keycard.repository;

import com.keycard.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT DISTINCT c FROM Card c JOIN c.tags t WHERE t.name = :tagName ORDER BY c.updatedAt DESC")
    List<Card> findByTagName(@Param("tagName") String tagName);

    @Query("SELECT c FROM Card c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.updatedAt DESC")
    List<Card> search(@Param("keyword") String keyword);

    List<Card> findAllByOrderByUpdatedAtDesc();
}
