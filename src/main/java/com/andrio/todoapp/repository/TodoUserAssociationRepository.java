package com.andrio.todoapp.repository;

import com.andrio.todoapp.model.Status;
import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.model.TodoUserAssociation;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface TodoUserAssociationRepository extends JpaRepository<TodoUserAssociation, Long> {

}
