package com.generator;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UsageRepository extends CrudRepository<Usage, Integer> {
    Usage findById(Integer id);

    @Query(value = "SELECT COUNT(user_id) FROM usage_log as ul WHERE ul.user_id =  ?1", nativeQuery = true)
    Integer countUsage(Integer User_id);

    List<Usage> findAllByUserId(Integer User_id);
}