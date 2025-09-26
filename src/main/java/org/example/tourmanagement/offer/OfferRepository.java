package org.example.tourmanagement.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    @Query("SELECT o FROM Offer o WHERE o.isActive = true")
    List<Offer> findByIsActiveTrue();

    @Query("SELECT o FROM Offer o WHERE o.isActive = true AND (o.startDate IS NULL OR o.startDate <= :today) AND (o.endDate IS NULL OR o.endDate >= :today)")
    List<Offer> findActiveAndCurrent(@Param("today") LocalDate today);

    @Query("SELECT o FROM Offer o WHERE o.promoCode = :promoCode AND o.isActive = true AND (o.startDate IS NULL OR o.startDate <= :today) AND (o.endDate IS NULL OR o.endDate >= :today)")
    Offer findByPromoCodeAndActiveAndCurrent(@Param("promoCode") String promoCode, @Param("today") LocalDate today);
}




