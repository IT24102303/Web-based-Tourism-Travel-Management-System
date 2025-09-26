package org.example.tourmanagement.admin;

import org.example.tourmanagement.offer.Offer;
import org.example.tourmanagement.offer.OfferRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/offers")
public class AdminOfferController {

    private final OfferRepository offerRepository;

    public AdminOfferController(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    @GetMapping
    public String manageOffers(Model model) {
        List<Offer> offers = offerRepository.findAll();
        model.addAttribute("offers", offers);
        model.addAttribute("newOffer", new Offer());
        return "manage-offers";
    }

    @PostMapping
    public String createOffer(@ModelAttribute("newOffer") Offer form, BindingResult bindingResult) {
        if (form.getTitle() == null || form.getTitle().isBlank()) {
            bindingResult.rejectValue("title", "required", "Title is required");
        }
        if (form.getDiscountedPrice() != null && form.getOriginalPrice() != null && form.getDiscountedPrice() > form.getOriginalPrice()) {
            bindingResult.rejectValue("discountedPrice", "invalid", "Discounted price must be less than original price");
        }
        
        // Validate promo code uniqueness if provided
        if (form.getPromoCode() != null && !form.getPromoCode().trim().isEmpty()) {
            form.setPromoCode(form.getPromoCode().trim().toUpperCase());
            if (offerRepository.findByPromoCodeAndActiveAndCurrent(form.getPromoCode(), LocalDate.now()) != null) {
                bindingResult.rejectValue("promoCode", "duplicate", "Promo code already exists");
            }
        }
        
        if (bindingResult.hasErrors()) {
            return "redirect:/admin/offers?error";
        }

        if (form.getOriginalPrice() != null && form.getDiscountedPrice() != null) {
            double percent = 100.0 - ((form.getDiscountedPrice() / form.getOriginalPrice()) * 100.0);
            form.setDiscountPercent((int) Math.round(percent));
        }
        if (form.getStartDate() == null) {
            form.setStartDate(LocalDate.now());
        }
        if (form.getIsActive() == null) {
            form.setIsActive(true);
        }
        offerRepository.save(form);
        return "redirect:/admin/offers?created";
    }

    @PostMapping("/{id}/update")
    public String updateOffer(@PathVariable("id") Long id, @ModelAttribute("offer") Offer form, BindingResult bindingResult) {
        Offer offer = offerRepository.findById(id).orElse(null);
        if (offer == null) {
            return "redirect:/admin/offers?notfound";
        }
        if (form.getTitle() == null || form.getTitle().isBlank()) {
            bindingResult.rejectValue("title", "required", "Title is required");
        }
        if (form.getDiscountedPrice() != null && form.getOriginalPrice() != null && form.getDiscountedPrice() > form.getOriginalPrice()) {
            bindingResult.rejectValue("discountedPrice", "invalid", "Discounted price must be less than original price");
        }
        
        // Validate promo code uniqueness if provided
        if (form.getPromoCode() != null && !form.getPromoCode().trim().isEmpty()) {
            form.setPromoCode(form.getPromoCode().trim().toUpperCase());
            Offer existingOffer = offerRepository.findByPromoCodeAndActiveAndCurrent(form.getPromoCode(), LocalDate.now());
            if (existingOffer != null && !existingOffer.getId().equals(offer.getId())) {
                bindingResult.rejectValue("promoCode", "duplicate", "Promo code already exists");
            }
        }
        
        if (bindingResult.hasErrors()) {
            return "redirect:/admin/offers?error";
        }

        offer.setTitle(form.getTitle());
        offer.setDescription(form.getDescription());
        offer.setImageUrl(form.getImageUrl());
        offer.setOriginalPrice(form.getOriginalPrice());
        offer.setDiscountedPrice(form.getDiscountedPrice());
        offer.setDiscountPercent(form.getDiscountPercent());
        offer.setStartDate(form.getStartDate());
        offer.setEndDate(form.getEndDate());
        offer.setPromoCode(form.getPromoCode());
        offer.setIsActive(form.getIsActive() == null ? true : form.getIsActive());

        if (offer.getOriginalPrice() != null && offer.getDiscountedPrice() != null) {
            double percent = 100.0 - ((offer.getDiscountedPrice() / offer.getOriginalPrice()) * 100.0);
            offer.setDiscountPercent((int) Math.round(percent));
        }

        offerRepository.save(offer);
        return "redirect:/admin/offers?updated";
    }

    @PostMapping("/{id}/delete")
    public String deleteOffer(@PathVariable("id") Long id) {
        offerRepository.findById(id).ifPresent(offerRepository::delete);
        return "redirect:/admin/offers?deleted";
    }
}




