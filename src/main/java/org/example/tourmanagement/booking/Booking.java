package org.example.tourmanagement.booking;

import org.example.tourmanagement.user.User;
import org.example.tourmanagement.destination.Destination;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @NotNull
    private LocalDate travelDate;

    @Min(value = 1, message = "Number of travelers must be at least 1")
    private Integer numberOfTravelers;

    @NotNull
    @Min(value = 0, message = "Total amount must be positive")
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @NotBlank
    @Size(min = 2, max = 100)
    private String contactName;

    @Email
    @NotBlank
    private String contactEmail;

    @Size(max = 20)
    private String contactPhone;

    @Size(max = 500)
    private String specialRequests;

    @Size(max = 500)
    private String paymentSlipUrl;

    @Size(max = 20)
    private String promoCode;

    private Double discountAmount = 0.0;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public Booking() {}

    public Booking(User user, Destination destination, LocalDate travelDate, 
                   Integer numberOfTravelers, String contactName, String contactEmail) {
        this.user = user;
        this.destination = destination;
        this.travelDate = travelDate;
        this.numberOfTravelers = numberOfTravelers;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.totalAmount = destination.getPrice() * numberOfTravelers;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }

    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }

    public Integer getNumberOfTravelers() { return numberOfTravelers; }
    public void setNumberOfTravelers(Integer numberOfTravelers) { 
        this.numberOfTravelers = numberOfTravelers;
        if (this.destination != null && numberOfTravelers != null) {
            this.totalAmount = this.destination.getPrice() * numberOfTravelers;
        }
    }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getPaymentSlipUrl() { return paymentSlipUrl; }
    public void setPaymentSlipUrl(String paymentSlipUrl) { this.paymentSlipUrl = paymentSlipUrl; }

    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void calculateTotalAmount() {
        if (this.destination != null && this.numberOfTravelers != null) {
            double baseAmount = this.destination.getPrice() * this.numberOfTravelers;
            this.totalAmount = baseAmount - (this.discountAmount != null ? this.discountAmount : 0.0);
        }
    }

    public void applyDiscount(Double discountAmount) {
        this.discountAmount = discountAmount;
        calculateTotalAmount();
    }
}
