package com.generator;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserRepository extends CrudRepository<User, Integer> {
    User findByEmail(String email);

    User findByToken(String token);

    @Query(value = "SELECT id FROM user as u WHERE u.email =  ?1", nativeQuery = true)
    Integer getIdByEmail(String email);
}