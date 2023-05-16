package Lim.boardApp.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdTime;

    @LastModifiedDate
    @Column(updatable = false)
    private LocalDateTime modifiedTime;

    @PrePersist
    public void onPrePersist() {
        String customLocalDateTimeFormat = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime parsedCreatedTime = LocalDateTime.parse(customLocalDateTimeFormat, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.createdTime = parsedCreatedTime;
        this.modifiedTime = this.createdTime;
    }

    @PreUpdate
    public void onPreUpdate(){
        String customLocalDateTimeFormat = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime parsedModifiedTime = LocalDateTime.parse(customLocalDateTimeFormat, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.modifiedTime = parsedModifiedTime;
    }
}
