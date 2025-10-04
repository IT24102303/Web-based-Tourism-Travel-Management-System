package org.example.tourmanagement.web;

import org.example.tourmanagement.offer.Offer;
import org.example.tourmanagement.offer.OfferRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class OfferController {

    private final OfferRepository offerRepository;

    public OfferController(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    @GetMapping("/offers")
    public String offers(Model model) {
        List<Offer> offers = offerRepository.findByIsActiveTrue();
        model.addAttribute("offers", offers);
        return "offers";
    }
}






