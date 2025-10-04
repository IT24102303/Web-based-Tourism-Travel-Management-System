package org.example.tourmanagement.web;

import org.example.tourmanagement.destination.Destination;
import org.example.tourmanagement.destination.DestinationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DestinationController {

    private final DestinationRepository destinationRepository;

    public DestinationController(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    @GetMapping("/destinations")
    public String destinations(Model model, 
                              @RequestParam(value = "region", required = false) String region,
                              @RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "minPrice", required = false) Double minPrice,
                              @RequestParam(value = "maxPrice", required = false) Double maxPrice) {
        
        List<Destination> destinations;
        
        if (search != null && !search.trim().isEmpty()) {
            destinations = destinationRepository.findBySearchTermAndIsActiveTrue(search.trim());
        } else if (region != null && !region.trim().isEmpty()) {
            destinations = destinationRepository.findByRegionAndIsActiveTrue(region.trim());
        } else if (minPrice != null && maxPrice != null) {
            destinations = destinationRepository.findByPriceRangeAndIsActiveTrue(minPrice, maxPrice);
        } else {
            destinations = destinationRepository.findByIsActiveTrue();
        }
        
        model.addAttribute("destinations", destinations);
        model.addAttribute("selectedRegion", region);
        model.addAttribute("searchTerm", search);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        
        // Add available regions for filter dropdown
        List<String> regions = destinationRepository.findByIsActiveTrue().stream()
                .map(Destination::getRegion)
                .distinct()
                .sorted()
                .toList();
        model.addAttribute("regions", regions);
        
        return "destinations";
    }
}







<<<<<<< HEAD
=======
<<<<<<< HEAD
=======

>>>>>>> c92fc5034a150c6425e19bd3f1caea9faea5523b
>>>>>>> d7b655a2b8838a35ac9f6bd57b5efaf547ee1fbb
