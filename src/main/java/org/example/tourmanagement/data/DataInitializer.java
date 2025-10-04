package org.example.tourmanagement.data;

import org.example.tourmanagement.destination.Destination;
import org.example.tourmanagement.destination.DestinationRepository;
import org.example.tourmanagement.booking.Booking;
import org.example.tourmanagement.booking.BookingRepository;
import org.example.tourmanagement.booking.BookingStatus;
import org.example.tourmanagement.user.User;
import org.example.tourmanagement.user.UserRepository;
import org.example.tourmanagement.offer.Offer;
import org.example.tourmanagement.offer.OfferRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DestinationRepository destinationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;

    public DataInitializer(DestinationRepository destinationRepository, BookingRepository bookingRepository, UserRepository userRepository, OfferRepository offerRepository) {
        this.destinationRepository = destinationRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only add sample data if no destinations exist
        if (destinationRepository.count() == 0) {
            createSampleDestinations();
        }
        
        // Create sample bookings for testing
        if (bookingRepository.count() == 0) {
            createSampleBookings();
        }
        
        // Create sample offers for testing
        if (offerRepository.count() == 0) {
            createSampleOffers();
        }
    }

    private void createSampleDestinations() {
        // Sample destination data
        Destination[] destinations = {
            new Destination("Bali, Indonesia", 
                "Experience the enchanting beauty of Bali with its pristine beaches, ancient temples, and vibrant culture. Perfect for relaxation and adventure seekers.", 
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80", 
                "Southeast Asia", 899.0),
            
            new Destination("Santorini, Greece", 
                "Discover the stunning white-washed buildings and breathtaking sunsets of Santorini. A romantic paradise in the Mediterranean.", 
                "https://i.pinimg.com/1200x/f2/36/a2/f236a27daa7d5b774cef01a96f246d37.jpg", 
                "Mediterranean", 1299.0),
            
            new Destination("Kyoto, Japan", 
                "Immerse yourself in traditional Japanese culture with ancient temples, beautiful gardens, and authentic experiences in Kyoto.", 
                "https://images.unsplash.com/photo-1513326738677-b964603b136d?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80", 
                "East Asia", 1499.0),
            
            new Destination("Paris, France", 
                "The City of Light offers iconic landmarks, world-class cuisine, and romantic experiences along the Seine River.", 
                "https://images.unsplash.com/photo-1513635269976-35436a86f8dc?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80", 
                "Europe", 1199.0),
            
            new Destination("Cape Town, South Africa", 
                "Explore the dramatic coastline, visit Table Mountain, and experience the rich cultural heritage of Cape Town.", 
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80", 
                "Africa", 1399.0),
            
            new Destination("New York City, USA", 
                "Experience the energy of the Big Apple with Broadway shows, iconic landmarks, and diverse neighborhoods.", 
                "https://images.unsplash.com/photo-1518241353608-43d6b579d404?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80", 
                "North America", 999.0),
            
            new Destination("Machu Picchu, Peru", 
                "Journey to the ancient Inca citadel high in the Andes mountains. A once-in-a-lifetime archaeological adventure.", 
                "https://images.unsplash.com/photo-1526772662000-3f88f10405ff?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80", 
                "South America", 1599.0),
            
            new Destination("Venice, Italy", 
                "Glide through the canals of Venice in a gondola and explore the romantic city's historic architecture and art.", 
                "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80", 
                "Europe", 1249.0)
        };

        // Set additional properties for each destination
        destinations[0].setRating(4.9);
        destinations[0].setReviewCount(2400);
        destinations[0].setBadge("Trending");

        destinations[1].setRating(4.8);
        destinations[1].setReviewCount(1900);

        destinations[2].setRating(4.9);
        destinations[2].setReviewCount(2100);
        destinations[2].setBadge("Popular");

        destinations[3].setRating(4.7);
        destinations[3].setReviewCount(3500);

        destinations[4].setRating(4.8);
        destinations[4].setReviewCount(1200);
        destinations[4].setBadge("Adventure");

        destinations[5].setRating(4.6);
        destinations[5].setReviewCount(2800);

        destinations[6].setRating(4.9);
        destinations[6].setReviewCount(1500);
        destinations[6].setBadge("Historic");

        destinations[7].setRating(4.7);
        destinations[7].setReviewCount(2300);

        // Save all destinations
        for (Destination destination : destinations) {
            destination.setIsActive(true);
            destinationRepository.save(destination);
        }
    }

    private void createSampleBookings() {
        // Create a test user if it doesn't exist
        User testUser = userRepository.findByEmail("test@example.com").orElse(null);
        if (testUser == null) {
            testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");
            testUser.setPasswordHash("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // password: test123
            testUser.setRole("USER");
            userRepository.save(testUser);
        }

        // Get some destinations to create bookings for
        Destination bali = destinationRepository.findByIsActiveTrue().stream()
                .filter(d -> d.getName().contains("Bali"))
                .findFirst().orElse(null);
        
        Destination paris = destinationRepository.findByIsActiveTrue().stream()
                .filter(d -> d.getName().contains("Paris"))
                .findFirst().orElse(null);

        if (bali != null) {
            // Create a pending booking for testing
            Booking booking1 = new Booking();
            booking1.setUser(testUser);
            booking1.setDestination(bali);
            booking1.setTravelDate(java.time.LocalDate.now().plusDays(30));
            booking1.setNumberOfTravelers(2);
            booking1.setContactName("John Doe");
            booking1.setContactEmail("john@example.com");
            booking1.setContactPhone("+1-555-123-4567");
            booking1.setSpecialRequests("Vegetarian meals please");
            booking1.setStatus(BookingStatus.PENDING);
            booking1.calculateTotalAmount();
            bookingRepository.save(booking1);
        }

        if (paris != null) {
            // Create another pending booking for testing
            Booking booking2 = new Booking();
            booking2.setUser(testUser);
            booking2.setDestination(paris);
            booking2.setTravelDate(java.time.LocalDate.now().plusDays(45));
            booking2.setNumberOfTravelers(1);
            booking2.setContactName("Jane Smith");
            booking2.setContactEmail("jane@example.com");
            booking2.setStatus(BookingStatus.PENDING);
            booking2.calculateTotalAmount();
            bookingRepository.save(booking2);
        }
    }
    
    private void createSampleOffers() {
        // Create sample offers
        Offer offer1 = new Offer();
        offer1.setTitle("Summer Sale 30% Off");
        offer1.setDescription("Get 30% off on all summer destinations. Perfect time to plan your vacation!");
        offer1.setImageUrl("https://images.unsplash.com/photo-1469474968028-56623f02e42e?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80");
        offer1.setOriginalPrice(1500.0);
        offer1.setDiscountedPrice(1050.0);
        offer1.setDiscountPercent(30);
        offer1.setStartDate(java.time.LocalDate.now());
        offer1.setEndDate(java.time.LocalDate.now().plusDays(30));
        offer1.setPromoCode("SUMMER30");
        offer1.setIsActive(true);
        offerRepository.save(offer1);
        
        Offer offer2 = new Offer();
        offer2.setTitle("Early Bird Special");
        offer2.setDescription("Book your next trip 60 days in advance and save 25% on your booking!");
        offer2.setImageUrl("https://images.unsplash.com/photo-1488646953014-85cb44e25828?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80");
        offer2.setOriginalPrice(2000.0);
        offer2.setDiscountedPrice(1500.0);
        offer2.setDiscountPercent(25);
        offer2.setStartDate(java.time.LocalDate.now());
        offer2.setEndDate(java.time.LocalDate.now().plusDays(60));
        offer2.setPromoCode("EARLY25");
        offer2.setIsActive(true);
        offerRepository.save(offer2);
        
        Offer offer3 = new Offer();
        offer3.setTitle("Weekend Getaway");
        offer3.setDescription("Special weekend packages with 20% discount on short trips!");
        offer3.setImageUrl("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80");
        offer3.setOriginalPrice(800.0);
        offer3.setDiscountedPrice(640.0);
        offer3.setDiscountPercent(20);
        offer3.setStartDate(java.time.LocalDate.now());
        offer3.setEndDate(java.time.LocalDate.now().plusDays(14));
        offer3.setPromoCode("WEEKEND20");
        offer3.setIsActive(true);
        offerRepository.save(offer3);
    }
}
