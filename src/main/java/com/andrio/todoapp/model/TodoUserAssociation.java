package com.andrio.todoapp.model;

import com.andrio.todoapp.dto.TodoUserDTO;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "todos_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoUserAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "todo_id")
    private Long todoId;

    @Column(name = "user_id")
    private Long todoUserId;

//    @Transient
//    @JsonProperty("user")
//    private TodoUserDTO todoUserDTO;

//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    @JsonIgnore
//    private TodoUser todoUser;

    @Column
    private Short role;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

//    @PostLoad
//    private void fillDTO() {
//        this.todoUserDTO = TodoUserDTO.builder()
//                .id(this.todoUser.getId())
//                .name(this.todoUser.getName())
//                .build();
//    }
}