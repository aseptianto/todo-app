package com.andrio.todoapp.repository;

import com.andrio.todoapp.model.UsersActivityLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersActivityLogRepository extends JpaRepository<UsersActivityLogs, Long> {
}