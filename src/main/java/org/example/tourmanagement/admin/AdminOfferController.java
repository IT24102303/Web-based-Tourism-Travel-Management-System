package org.example.tourmanagement.admin;

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
    public String createOffer(@ModelAttribute("newOffer") Offer form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // Title validation
        if (form.getTitle() == null || form.getTitle().trim().isEmpty()) {
            bindingResult.rejectValue("title", "required", "Title is required");
        } else if (form.getTitle().trim().length() < 3) {
            bindingResult.rejectValue("title", "minlength", "Title must be at least 3 characters long");
        } else if (form.getTitle().trim().length() > 100) {
            bindingResult.rejectValue("title", "maxlength", "Title must not exceed 100 characters");
        }
        
        // Description validation
        if (form.getDescription() == null || form.getDescription().trim().isEmpty()) {
            bindingResult.rejectValue("description", "required", "Description is required");
        } else if (form.getDescription().trim().length() < 10) {
            bindingResult.rejectValue("description", "minlength", "Description must be at least 10 characters long");
        } else if (form.getDescription().trim().length() > 500) {
            bindingResult.rejectValue("description", "maxlength", "Description must not exceed 500 characters");
        }
        
        // Image URL validation
        if (form.getImageUrl() == null || form.getImageUrl().trim().isEmpty()) {
            bindingResult.rejectValue("imageUrl", "required", "Image URL is required");
        } else if (!isValidUrl(form.getImageUrl().trim())) {
            bindingResult.rejectValue("imageUrl", "invalid", "Please enter a valid image URL");
        }
        
        // Price validation
        if (form.getOriginalPrice() == null) {
            bindingResult.rejectValue("originalPrice", "required", "Original price is required");
        } else if (form.getOriginalPrice() <= 0) {
            bindingResult.rejectValue("originalPrice", "min", "Original price must be greater than 0");
        }
        
        if (form.getDiscountedPrice() == null) {
            bindingResult.rejectValue("discountedPrice", "required", "Discounted price is required");
        } else if (form.getDiscountedPrice() <= 0) {
            bindingResult.rejectValue("discountedPrice", "min", "Discounted price must be greater than 0");
        } else if (form.getOriginalPrice() != null && form.getDiscountedPrice() >= form.getOriginalPrice()) {
            bindingResult.rejectValue("discountedPrice", "invalid", "Discounted price must be less than original price");
        }
        
        // Date validation
        if (form.getStartDate() == null) {
            bindingResult.rejectValue("startDate", "required", "Start date is required");
        } else if (form.getStartDate().isBefore(LocalDate.now())) {
            bindingResult.rejectValue("startDate", "past", "Start date cannot be in the past");
        }
        
        if (form.getEndDate() == null) {
            bindingResult.rejectValue("endDate", "required", "End date is required");
        } else if (form.getStartDate() != null && form.getEndDate().isBefore(form.getStartDate())) {
            bindingResult.rejectValue("endDate", "invalid", "End date must be after start date");
        }
        
        // Promo code validation
        if (form.getPromoCode() != null && !form.getPromoCode().trim().isEmpty()) {
            String promoCode = form.getPromoCode().trim().toUpperCase();
            if (promoCode.length() < 3) {
                bindingResult.rejectValue("promoCode", "minlength", "Promo code must be at least 3 characters long");
            } else if (promoCode.length() > 20) {
                bindingResult.rejectValue("promoCode", "maxlength", "Promo code must not exceed 20 characters");
            } else if (!promoCode.matches("^[A-Z0-9]+$")) {
                bindingResult.rejectValue("promoCode", "invalid", "Promo code can only contain letters and numbers");
            } else {
                form.setPromoCode(promoCode);
                if (offerRepository.findByPromoCodeAndActiveAndCurrent(promoCode, LocalDate.now()) != null) {
                    bindingResult.rejectValue("promoCode", "duplicate", "Promo code already exists");
                }
            }
        }
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the form errors and try again");
            return "redirect:/admin/offers";
        }

        // Calculate discount percentage
        if (form.getOriginalPrice() != null && form.getDiscountedPrice() != null) {
            double percent = 100.0 - ((form.getDiscountedPrice() / form.getOriginalPrice()) * 100.0);
            form.setDiscountPercent((int) Math.round(percent));
        }
        
        // Set default values
        if (form.getIsActive() == null) {
            form.setIsActive(true);
        }
        
        try {
            offerRepository.save(form);
            redirectAttributes.addFlashAttribute("success", "Offer created successfully");
            return "redirect:/admin/offers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create offer: " + e.getMessage());
            return "redirect:/admin/offers";
        }
    }
    
    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
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
    public String deleteOffer(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        System.out.println("Delete offer called with ID: " + id);
        try {
            Offer offer = offerRepository.findById(id).orElse(null);
            if (offer == null) {
                System.out.println("Offer not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Offer not found");
                return "redirect:/admin/offers";
            }
            
            System.out.println("Deleting offer: " + offer.getTitle());
            offerRepository.delete(offer);
            System.out.println("Offer deleted successfully");
            redirectAttributes.addFlashAttribute("success", "Offer deleted successfully");
            return "redirect:/admin/offers";
        } catch (Exception e) {
            System.out.println("Error deleting offer: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete offer: " + e.getMessage());
            return "redirect:/admin/offers";
        }
    }
}




