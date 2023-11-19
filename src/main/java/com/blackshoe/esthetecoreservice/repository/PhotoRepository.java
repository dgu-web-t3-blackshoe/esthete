package com.blackshoe.esthetecoreservice.repository;

import com.blackshoe.esthetecoreservice.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    @EntityGraph(attributePaths = "photoGenres")
    Optional<Photo> findByPhotoId(UUID photoId);
}
