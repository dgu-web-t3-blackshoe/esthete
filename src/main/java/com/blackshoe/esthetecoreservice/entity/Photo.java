package com.blackshoe.esthetecoreservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bytebuddy.asm.Advice;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "photos")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter @Builder @AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "photo_id")
    private Long id;

    @Column(columnDefinition = "BINARY(16)", name = "photo_uuid")
    private UUID photoId;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description", nullable = true, length = 100)
    private String description;

    @JoinColumn(name = "photo_url_id", foreignKey = @ForeignKey(name = "photo_fk_photo_url_id"))
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private PhotoUrl photoUrl;

    @Column(name = "photo_time", nullable = false, length = 20)
    private LocalDateTime time;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "photo_location_id", foreignKey = @ForeignKey(name = "photo_fk_photo_location_id"))
    private PhotoLocation photoLocation;

    //equipments
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Equipment> equipments = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Genre> genres = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "photo_view_id", foreignKey = @ForeignKey(name = "photo_fk_photo_view_id"))
    private PhotoView photoView;

    @CreatedDate
    @Column(name = "created_at", length = 20)
    private LocalDateTime createdAt;

    public void setPhotoUrl(PhotoUrl photoUrl) {
        this.photoUrl = photoUrl;
    }

    @PrePersist
    public void setPhotoId() {
        if (photoId == null) {
            photoId = UUID.randomUUID();
        }
    }
}
