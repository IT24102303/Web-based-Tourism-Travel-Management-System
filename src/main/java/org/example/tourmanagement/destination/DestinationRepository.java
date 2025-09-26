package org.example.tourmanagement.destination;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    List<Destination> findByIsActiveTrue();

    List<Destination> findByRegionAndIsActiveTrue(String region);

    @Query("SELECT d FROM Destination d WHERE d.isActive = true AND d.price BETWEEN :min AND :max")
    List<Destination> findByPriceRangeAndIsActiveTrue(@Param("min") Double minPrice, @Param("max") Double maxPrice);

    @Query("SELECT d FROM Destination d WHERE d.isActive = true AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(d.region) LIKE LOWER(CONCAT('%', :term, '%'))) ")
    List<Destination> findBySearchTermAndIsActiveTrue(@Param("term") String term);

    @Query("SELECT d FROM Destination d WHERE d.isActive = true ORDER BY d.rating DESC, d.reviewCount DESC")
    List<Destination> findPopularDestinations();
}







