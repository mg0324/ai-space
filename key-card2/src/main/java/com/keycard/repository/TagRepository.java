package com.keycard.repository;

import com.keycard.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Tag> findAllByOrderByNameAsc();

    @Query("SELECT COUNT(c) FROM Card c JOIN c.tags t WHERE t.id = :tagId")
    long countCardsByTagId(@Param("tagId") Long tagId);
}
