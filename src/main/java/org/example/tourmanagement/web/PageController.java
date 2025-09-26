package org.example.tourmanagement.web;

import org.example.tourmanagement.destination.Destination;
import org.example.tourmanagement.destination.DestinationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PageController {
    
    private final DestinationRepository destinationRepository;
    
    public PageController(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }
    
    @GetMapping("/")
    public String home(Model model) {
        // Get top 3 popular destinations
        List<Destination> popularDestinations = destinationRepository.findPopularDestinations();
        if (popularDestinations.size() > 3) {
            popularDestinations = popularDestinations.subList(0, 3);
        }
        
        model.addAttribute("popularDestinations", popularDestinations);
        return "index";
    }
}


