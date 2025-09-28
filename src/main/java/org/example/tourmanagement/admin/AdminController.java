package org.example.tourmanagement.admin;

import org.example.tourmanagement.user.User;
import org.example.tourmanagement.user.UserRepository;
import org.example.tourmanagement.destination.Destination;
import org.example.tourmanagement.destination.DestinationRepository;
import org.example.tourmanagement.booking.Booking;
import org.example.tourmanagement.booking.BookingRepository;
import org.example.tourmanagement.booking.BookingStatus;
import org.example.tourmanagement.support.InquiryRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DestinationRepository destinationRepository;
    private final BookingRepository bookingRepository;
    private final InquiryRepository inquiryRepository;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder, DestinationRepository destinationRepository, BookingRepository bookingRepository, InquiryRepository inquiryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.destinationRepository = destinationRepository;
        this.bookingRepository = bookingRepository;
        this.inquiryRepository = inquiryRepository;
    }

    @GetMapping
    public String admin(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("newUser", new CreateUserForm());
        
        // Calculate analytics data
        calculateAnalytics(model);
        
        return "admin";
    }
    
    private void calculateAnalytics(Model model) {
        // Total users count
        long totalUsers = userRepository.count();
        model.addAttribute("totalUsers", totalUsers);
        
        // Active bookings count (APPROVED status)
        long activeBookings = bookingRepository.countByStatus(BookingStatus.APPROVED);
        model.addAttribute("activeBookings", activeBookings);
        
        // Support tickets count (all inquiries)
        long supportTickets = inquiryRepository.count();
        model.addAttribute("supportTickets", supportTickets);
        
        // Revenue calculation for current month
        BigDecimal monthlyRevenue = calculateMonthlyRevenue();
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        
        // Additional analytics
        long totalBookings = bookingRepository.count();
        long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        long rejectedBookings = bookingRepository.countByStatus(BookingStatus.REJECTED);
        long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("rejectedBookings", rejectedBookings);
        model.addAttribute("completedBookings", completedBookings);
    }
    
    private BigDecimal calculateMonthlyRevenue() {
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();
        
        List<Booking> monthlyBookings = bookingRepository.findByTravelDateBetween(startOfMonth, endOfMonth);
        
        double totalRevenue = monthlyBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.APPROVED || booking.getStatus() == BookingStatus.COMPLETED)
                .mapToDouble(booking -> {
                    if (booking.getDestination() != null && booking.getDestination().getPrice() != null) {
                        return booking.getDestination().getPrice();
                    }
                    return 0.0;
                })
                .sum();
        
        return BigDecimal.valueOf(totalRevenue);
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute("newUser") CreateUserForm form, BindingResult bindingResult) {
        if (form.getUsername() == null || form.getUsername().isBlank()) {
            bindingResult.rejectValue("username", "required", "Username is required");
        }
        if (form.getEmail() == null || form.getEmail().isBlank()) {
            bindingResult.rejectValue("email", "required", "Email is required");
        }
        if (form.getPassword() == null || form.getPassword().length() < 6) {
            bindingResult.rejectValue("password", "min", "Password must be at least 6 characters");
        }
        if (bindingResult.hasErrors()) {
            return "redirect:/admin?error";
        }
        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(form.getRole() == null || form.getRole().isBlank() ? "USER" : form.getRole().toUpperCase());
        userRepository.save(user);
        return "redirect:/admin?created";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable("id") Long id,
                             @ModelAttribute("user") UpdateUserForm form,
                             BindingResult bindingResult) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin?notfound";
        }
        if (form.getUsername() == null || form.getUsername().isBlank()) {
            bindingResult.rejectValue("username", "required", "Username is required");
        }
        if (form.getEmail() == null || form.getEmail().isBlank()) {
            bindingResult.rejectValue("email", "required", "Email is required");
        }
        if (bindingResult.hasErrors()) {
            return "redirect:/admin?error";
        }
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        if (form.getRole() != null && !form.getRole().isBlank()) {
            user.setRole(form.getRole().toUpperCase());
        }
        userRepository.save(user);
        return "redirect:/admin?updated";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
        return "redirect:/admin?deleted";
    }

    // Destination management endpoints
    @GetMapping("/destinations")
    public String manageDestinations(Model model) {
        List<Destination> destinations = destinationRepository.findAll();
        model.addAttribute("destinations", destinations);
        model.addAttribute("newDestination", new CreateDestinationForm());
        return "manage-destinations";
    }

    @PostMapping("/destinations")
    public String createDestination(@ModelAttribute("newDestination") CreateDestinationForm form, BindingResult bindingResult) {
        if (form.getName() == null || form.getName().isBlank()) {
            bindingResult.rejectValue("name", "required", "Destination name is required");
        }
        if (form.getDescription() == null || form.getDescription().isBlank()) {
            bindingResult.rejectValue("description", "required", "Description is required");
        }
        if (form.getImageUrl() == null || form.getImageUrl().isBlank()) {
            bindingResult.rejectValue("imageUrl", "required", "Image URL is required");
        }
        if (form.getRegion() == null || form.getRegion().isBlank()) {
            bindingResult.rejectValue("region", "required", "Region is required");
        }
        if (form.getPrice() == null || form.getPrice() <= 0) {
            bindingResult.rejectValue("price", "required", "Valid price is required");
        }
        if (bindingResult.hasErrors()) {
            return "redirect:/admin/destinations?error";
        }
        
        Destination destination = new Destination();
        destination.setName(form.getName());
        destination.setDescription(form.getDescription());
        destination.setImageUrl(form.getImageUrl());
        destination.setRegion(form.getRegion());
        destination.setPrice(form.getPrice());
        destination.setRating(form.getRating());
        destination.setReviewCount(form.getReviewCount());
        destination.setBadge(form.getBadge());
        destination.setIsActive(true);
        
        destinationRepository.save(destination);
        return "redirect:/admin/destinations?created";
    }

    @PostMapping("/destinations/{id}/update")
    public String updateDestination(@PathVariable("id") Long id,
                                   @ModelAttribute("destination") UpdateDestinationForm form,
                                   BindingResult bindingResult) {
        Destination destination = destinationRepository.findById(id).orElse(null);
        if (destination == null) {
            return "redirect:/admin/destinations?notfound";
        }
        if (form.getName() == null || form.getName().isBlank()) {
            bindingResult.rejectValue("name", "required", "Destination name is required");
        }
        if (form.getDescription() == null || form.getDescription().isBlank()) {
            bindingResult.rejectValue("description", "required", "Description is required");
        }
        if (form.getImageUrl() == null || form.getImageUrl().isBlank()) {
            bindingResult.rejectValue("imageUrl", "required", "Image URL is required");
        }
        if (form.getRegion() == null || form.getRegion().isBlank()) {
            bindingResult.rejectValue("region", "required", "Region is required");
        }
        if (form.getPrice() == null || form.getPrice() <= 0) {
            bindingResult.rejectValue("price", "required", "Valid price is required");
        }
        if (bindingResult.hasErrors()) {
            return "redirect:/admin/destinations?error";
        }
        
        destination.setName(form.getName());
        destination.setDescription(form.getDescription());
        destination.setImageUrl(form.getImageUrl());
        destination.setRegion(form.getRegion());
        destination.setPrice(form.getPrice());
        destination.setRating(form.getRating());
        destination.setReviewCount(form.getReviewCount());
        destination.setBadge(form.getBadge());
        if (form.getIsActive() != null) {
            destination.setIsActive(form.getIsActive());
        }
        
        destinationRepository.save(destination);
        return "redirect:/admin/destinations?updated";
    }

    @PostMapping("/destinations/{id}/delete")
    public String deleteDestination(@PathVariable("id") Long id) {
        destinationRepository.findById(id).ifPresent(destinationRepository::delete);
        return "redirect:/admin/destinations?deleted";
    }

    // Booking management endpoints
    @GetMapping("/bookings")
    public String manageBookings(Model model,
                                @RequestParam(value = "status", required = false) String status,
                                @RequestParam(value = "destination", required = false) Long destinationId) {
        List<Booking> bookings;
        
        if (status != null && !status.isEmpty()) {
            try {
                BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                bookings = bookingRepository.findByStatusOrderByCreatedAtDesc(bookingStatus);
            } catch (IllegalArgumentException e) {
                bookings = bookingRepository.findAll();
            }
        } else if (destinationId != null) {
            Destination destination = destinationRepository.findById(destinationId).orElse(null);
            if (destination != null) {
                bookings = bookingRepository.findByDestinationOrderByCreatedAtDesc(destination);
            } else {
                bookings = bookingRepository.findAll();
            }
        } else {
            bookings = bookingRepository.findAll();
        }
        
        List<Destination> destinations = destinationRepository.findByIsActiveTrue();
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("destinations", destinations);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDestination", destinationId);
        
        return "manage-bookings";
    }

    @PostMapping("/bookings/{id}/approve")
    public String approveBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        System.out.println("Approving booking with ID: " + id);
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            System.out.println("Booking not found with ID: " + id);
            redirectAttributes.addFlashAttribute("error", "Booking not found");
            return "redirect:/admin/bookings";
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            System.out.println("Booking status is not PENDING: " + booking.getStatus());
            redirectAttributes.addFlashAttribute("error", "Only pending bookings can be approved");
            return "redirect:/admin/bookings";
        }

        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
        System.out.println("Booking approved successfully");
        redirectAttributes.addFlashAttribute("success", "Booking approved successfully");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/bookings/{id}/reject")
    public String rejectBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        System.out.println("Rejecting booking with ID: " + id);
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            System.out.println("Booking not found with ID: " + id);
            redirectAttributes.addFlashAttribute("error", "Booking not found");
            return "redirect:/admin/bookings";
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            System.out.println("Booking status is not PENDING: " + booking.getStatus());
            redirectAttributes.addFlashAttribute("error", "Only pending bookings can be rejected");
            return "redirect:/admin/bookings";
        }

        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);
        System.out.println("Booking rejected successfully");
        redirectAttributes.addFlashAttribute("success", "Booking rejected successfully");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/bookings/{id}/update-status")
    public String updateBookingStatus(@PathVariable("id") Long id,
                                     @RequestParam("status") String status,
                                     RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("error", "Booking not found");
            return "redirect:/admin/bookings";
        }

        try {
            BookingStatus newStatus = BookingStatus.valueOf(status.toUpperCase());
            
            // Prevent invalid status transitions
            if (booking.getStatus() == BookingStatus.PENDING && 
                (newStatus == BookingStatus.CANCELLED || newStatus == BookingStatus.COMPLETED)) {
                redirectAttributes.addFlashAttribute("error", "Pending bookings must be approved or rejected first");
                return "redirect:/admin/bookings";
            }
            
            booking.setStatus(newStatus);
            bookingRepository.save(booking);
            redirectAttributes.addFlashAttribute("success", "Booking status updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid status");
        }

        return "redirect:/admin/bookings";
    }

    public static class CreateUserForm {
        private String username;
        private String email;
        private String password;
        private String role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class UpdateUserForm {
        private String username;
        private String email;
        private String password;
        private String role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class CreateDestinationForm {
        private String name;
        private String description;
        private String imageUrl;
        private String region;
        private Double price;
        private Double rating;
        private Integer reviewCount;
        private String badge;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        public Integer getReviewCount() { return reviewCount; }
        public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
        public String getBadge() { return badge; }
        public void setBadge(String badge) { this.badge = badge; }
    }

    public static class UpdateDestinationForm {
        private String name;
        private String description;
        private String imageUrl;
        private String region;
        private Double price;
        private Double rating;
        private Integer reviewCount;
        private String badge;
        private Boolean isActive;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        public Integer getReviewCount() { return reviewCount; }
        public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
        public String getBadge() { return badge; }
        public void setBadge(String badge) { this.badge = badge; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }
}


