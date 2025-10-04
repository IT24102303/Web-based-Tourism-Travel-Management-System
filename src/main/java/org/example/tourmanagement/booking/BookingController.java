package org.example.tourmanagement.booking;

import org.example.tourmanagement.user.User;
import org.example.tourmanagement.destination.Destination;
import org.example.tourmanagement.destination.DestinationRepository;
import org.example.tourmanagement.offer.Offer;
import org.example.tourmanagement.offer.OfferRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final DestinationRepository destinationRepository;
    private final OfferRepository offerRepository;

    public BookingController(BookingRepository bookingRepository, DestinationRepository destinationRepository, OfferRepository offerRepository) {
        this.bookingRepository = bookingRepository;
        this.destinationRepository = destinationRepository;
        this.offerRepository = offerRepository;
    }

    @GetMapping("/book/{destinationId}")
    public String showBookingForm(@PathVariable Long destinationId, Model model) {
        Destination destination = destinationRepository.findById(destinationId).orElse(null);
        if (destination == null || !destination.getIsActive()) {
            return "redirect:/destinations?error=notfound";
        }

        model.addAttribute("destination", destination);
        model.addAttribute("bookingForm", new BookingForm());
        return "booking-form";
    }

    @PostMapping("/book/{destinationId}")
    public String createBooking(@PathVariable Long destinationId,
                               @ModelAttribute("bookingForm") BookingForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        
        Destination destination = destinationRepository.findById(destinationId).orElse(null);
        if (destination == null || !destination.getIsActive()) {
            return "redirect:/destinations?error=notfound";
        }

        // Validation
        if (form.getTravelDate() == null) {
            bindingResult.rejectValue("travelDate", "required", "Travel date is required");
        } else if (form.getTravelDate().isBefore(LocalDate.now())) {
            bindingResult.rejectValue("travelDate", "invalid", "Travel date cannot be in the past");
        }

        if (form.getNumberOfTravelers() == null || form.getNumberOfTravelers() < 1) {
            bindingResult.rejectValue("numberOfTravelers", "required", "Number of travelers must be at least 1");
        }

        if (form.getContactName() == null || form.getContactName().trim().isEmpty()) {
            bindingResult.rejectValue("contactName", "required", "Contact name is required");
        }

        if (form.getContactEmail() == null || form.getContactEmail().trim().isEmpty()) {
            bindingResult.rejectValue("contactEmail", "required", "Contact email is required");
        }

        // Validate promo code if provided
        Offer promoOffer = null;
        if (form.getPromoCode() != null && !form.getPromoCode().trim().isEmpty()) {
            promoOffer = offerRepository.findByPromoCodeAndActiveAndCurrent(form.getPromoCode().trim(), LocalDate.now());
            if (promoOffer == null) {
                bindingResult.rejectValue("promoCode", "invalid", "Invalid or expired promo code");
            }
        }

        if (bindingResult.hasErrors()) {
            return "booking-form";
        }

        // Create booking
        Booking booking = new Booking();
        
        // For now, we'll create a dummy user since we don't have user authentication fully set up
        // In a real application, you'd get the current authenticated user
        User dummyUser = new User();
        dummyUser.setId(1L); // This should be the actual logged-in user's ID
        dummyUser.setUsername("current_user");
        dummyUser.setEmail(form.getContactEmail());
        
        booking.setUser(dummyUser);
        booking.setDestination(destination);
        booking.setTravelDate(form.getTravelDate());
        booking.setNumberOfTravelers(form.getNumberOfTravelers());
        booking.setContactName(form.getContactName());
        booking.setContactEmail(form.getContactEmail());
        booking.setContactPhone(form.getContactPhone());
        booking.setSpecialRequests(form.getSpecialRequests());
        booking.setPaymentSlipUrl(form.getPaymentSlipUrl());
        booking.setPromoCode(form.getPromoCode());
        
        // Apply discount if promo code is valid
        if (promoOffer != null) {
            double baseAmount = destination.getPrice() * form.getNumberOfTravelers();
            double discountAmount = 0.0;
            
            if (promoOffer.getDiscountPercent() != null) {
                discountAmount = baseAmount * (promoOffer.getDiscountPercent() / 100.0);
            } else if (promoOffer.getDiscountedPrice() != null && promoOffer.getOriginalPrice() != null) {
                double discountPercent = 100.0 - ((promoOffer.getDiscountedPrice() / promoOffer.getOriginalPrice()) * 100.0);
                discountAmount = baseAmount * (discountPercent / 100.0);
            }
            
            booking.setDiscountAmount(discountAmount);
        }
        
        booking.calculateTotalAmount();

        bookingRepository.save(booking);

        redirectAttributes.addFlashAttribute("success", "Booking created successfully! Booking ID: " + booking.getId());
        return "redirect:/my-bookings";
    }

    @GetMapping("/my-bookings")
    public String myBookings(Model model) {
        // For now, we'll get bookings for user ID 1 (dummy user)
        // In a real application, you'd get the current authenticated user
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(1L);
        
        LocalDate today = LocalDate.now();
        List<Booking> upcomingBookings = bookings.stream()
                .filter(booking -> booking.getTravelDate().isAfter(today) || booking.getTravelDate().isEqual(today))
                .toList();
        
        List<Booking> pastBookings = bookings.stream()
                .filter(booking -> booking.getTravelDate().isBefore(today))
                .toList();

        model.addAttribute("bookings", bookings);
        model.addAttribute("upcomingBookings", upcomingBookings);
        model.addAttribute("pastBookings", pastBookings);
        return "my-bookings";
    }

    @GetMapping("/bookings/{id}/details")
    public String viewBookingDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("error", "Booking not found");
            return "redirect:/my-bookings";
        }

        // Check if the booking belongs to the current user (for now, user ID 1)
        if (!booking.getUser().getId().equals(1L)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to view this booking");
            return "redirect:/my-bookings";
        }

        model.addAttribute("booking", booking);
        return "booking-details";
    }

    @PostMapping("/bookings/{id}/delete")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("error", "Booking not found");
            return "redirect:/my-bookings";
        }

        // Check if the booking belongs to the current user (for now, user ID 1)
        if (!booking.getUser().getId().equals(1L)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this booking");
            return "redirect:/my-bookings";
        }

        // Only allow deletion of cancelled bookings
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            redirectAttributes.addFlashAttribute("error", "Only cancelled bookings can be deleted");
            return "redirect:/my-bookings";
        }

        try {
            bookingRepository.delete(booking);
            redirectAttributes.addFlashAttribute("success", "Booking deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete booking: " + e.getMessage());
        }

        return "redirect:/my-bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("error", "Booking not found");
            return "redirect:/my-bookings";
        }

        // Check if the booking belongs to the current user (for now, user ID 1)
        if (!booking.getUser().getId().equals(1L)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to cancel this booking");
            return "redirect:/my-bookings";
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            redirectAttributes.addFlashAttribute("error", "Booking is already cancelled");
            return "redirect:/my-bookings";
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            redirectAttributes.addFlashAttribute("error", "Only pending bookings can be cancelled");
            return "redirect:/my-bookings";
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully");
        return "redirect:/my-bookings";
    }

    // Form class for booking
    public static class BookingForm {
        private LocalDate travelDate;
        private Integer numberOfTravelers;
        private String contactName;
        private String contactEmail;
        private String contactPhone;
        private String specialRequests;
        private String paymentSlipUrl;
        private String promoCode;

        public LocalDate getTravelDate() { return travelDate; }
        public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }

        public Integer getNumberOfTravelers() { return numberOfTravelers; }
        public void setNumberOfTravelers(Integer numberOfTravelers) { this.numberOfTravelers = numberOfTravelers; }

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
    }
}
