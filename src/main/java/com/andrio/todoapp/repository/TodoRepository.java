package com.andrio.todoapp.repository;

import com.andrio.todoapp.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    @Query("SELECT t FROM Todo t JOIN TodoUserAssociation tua ON t.id = tua.todoId WHERE tua.todoUserId = :userId")
    List<Todo> findByUserId(Long userId);
}
