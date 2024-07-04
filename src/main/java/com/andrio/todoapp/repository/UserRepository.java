package com.andrio.todoapp.repository;

import com.andrio.todoapp.model.TodoUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<TodoUser, Long> {

    TodoUser findByEmail(String email);
}
