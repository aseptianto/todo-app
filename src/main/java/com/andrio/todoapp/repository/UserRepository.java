package com.andrio.todoapp.repository;

import com.andrio.todoapp.model.TodoUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<TodoUser, Long> {

    TodoUser findByEmail(String email);

    @Query("SELECT u.id FROM TodoUser u where u.id IN :userIds")
    List<Long> findUserIdsByIds(@Param("userIds") List<Long> userIds);
}
