package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audits")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Audits {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "modified_by")
    private Employee modifiedBy;

    private LocalDateTime modifiedAt;

    private String tableName;

    private String actionType;

}