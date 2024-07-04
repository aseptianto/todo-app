package com.andrio.todoapp.repository;

import com.andrio.todoapp.model.TodoUserAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TodoUserAssociationRepository extends JpaRepository<TodoUserAssociation, Long> {

}
