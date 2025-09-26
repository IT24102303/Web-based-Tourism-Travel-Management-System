package org.example.tourmanagement.booking;

import org.example.tourmanagement.user.User;
import org.example.tourmanagement.destination.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Find all bookings by user
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    
    // Find bookings by user ID
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find bookings by status
    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);
    
    // Find bookings by destination
    List<Booking> findByDestinationOrderByCreatedAtDesc(Destination destination);
    
    // Find bookings within date range
    @Query("SELECT b FROM Booking b WHERE b.travelDate BETWEEN :startDate AND :endDate ORDER BY b.travelDate DESC")
    List<Booking> findByTravelDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Find upcoming bookings (travel date in future)
    @Query("SELECT b FROM Booking b WHERE b.travelDate >= :today ORDER BY b.travelDate ASC")
    List<Booking> findUpcomingBookings(@Param("today") LocalDate today);
    
    // Find upcoming bookings for a specific user
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.travelDate >= :today ORDER BY b.travelDate ASC")
    List<Booking> findUpcomingBookingsByUser(@Param("user") User user, @Param("today") LocalDate today);
    
    // Find past bookings for a specific user
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.travelDate < :today ORDER BY b.travelDate DESC")
    List<Booking> findPastBookingsByUser(@Param("user") User user, @Param("today") LocalDate today);
    
    // Count bookings by status
    long countByStatus(BookingStatus status);
    
    // Count bookings by user
    long countByUser(User user);
    
    // Find recent bookings (last 30 days)
    @Query("SELECT b FROM Booking b WHERE b.createdAt >= :thirtyDaysAgo ORDER BY b.createdAt DESC")
    List<Booking> findRecentBookings(@Param("thirtyDaysAgo") java.time.LocalDateTime thirtyDaysAgo);
}






