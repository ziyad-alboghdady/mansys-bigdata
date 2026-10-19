package org.sau.mansys.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@Entity
@Table(name = "supervise")
public class Supervise implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String student;

    private Integer performance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "adviser_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Adviser adviser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "study_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Study study;
}