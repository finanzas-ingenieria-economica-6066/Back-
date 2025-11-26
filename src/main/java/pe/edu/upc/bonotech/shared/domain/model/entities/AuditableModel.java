package pe.edu.upc.bonotech.shared.domain.model.entities;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class AuditableModel {
    @Getter
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;  

    @Getter
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}