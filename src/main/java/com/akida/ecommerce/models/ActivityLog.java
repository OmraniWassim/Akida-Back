package com.akida.ecommerce.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "activity_logs")
public class ActivityLog extends BasicEntity{
    @Id
    @SequenceGenerator(name="activity_log_sequence",sequenceName ="activity_log_sequence",allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "activity_log_sequence")
    private Long id;

    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String targetEntity;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}