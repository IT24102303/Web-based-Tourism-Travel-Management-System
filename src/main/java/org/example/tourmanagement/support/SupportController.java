package org.example.tourmanagement.support;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SupportController {

    private final InquiryRepository inquiryRepository;

    public SupportController(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    @GetMapping("/customer-support")
    public String customerSupport(@RequestParam(value = "email", required = false) String email,
                                  jakarta.servlet.http.HttpSession session,
                                  Model model) {
        model.addAttribute("inquiry", new Inquiry());
        String sessionEmail = (String) session.getAttribute("supportEmail");
        String effectiveEmail = email != null && !email.isBlank() ? email : sessionEmail;
        if (effectiveEmail != null && !effectiveEmail.isBlank()) {
            model.addAttribute("myTickets", inquiryRepository.findByEmailOrderByCreatedAtDesc(effectiveEmail));
            model.addAttribute("myEmail", effectiveEmail);
        }
        return "customer-support";
    }

    @PostMapping("/customer-support")
    public String submitInquiry(@ModelAttribute("inquiry") Inquiry inquiry, BindingResult bindingResult, jakarta.servlet.http.HttpSession session) {
        if (inquiry.getName() == null || inquiry.getName().isBlank()) {
            bindingResult.rejectValue("name", "required", "Name is required");
        }
        if (inquiry.getEmail() == null || inquiry.getEmail().isBlank()) {
            bindingResult.rejectValue("email", "required", "Email is required");
        }
        if (inquiry.getSubject() == null || inquiry.getSubject().isBlank()) {
            bindingResult.rejectValue("subject", "required", "Subject is required");
        }
        if (inquiry.getMessage() == null || inquiry.getMessage().isBlank()) {
            bindingResult.rejectValue("message", "required", "Message is required");
        }
        if (bindingResult.hasErrors()) {
            return "customer-support";
        }
        inquiry.setStatus(InquiryStatus.OPEN);
        inquiryRepository.save(inquiry);
        session.setAttribute("supportEmail", inquiry.getEmail());
        return "redirect:/customer-support?submitted";
    }

    @PostMapping("/customer-support/{id}/delete")
    public String deleteInquiry(@PathVariable("id") Long id,
                                @RequestParam(value = "email", required = false) String email,
                                jakarta.servlet.http.HttpSession session,
                                RedirectAttributes redirectAttributes) {
        inquiryRepository.findById(id).ifPresent(inquiry -> {
            String sessionEmail = (String) session.getAttribute("supportEmail");
            String ownerEmail = inquiry.getEmail();
            String effectiveEmail = email != null && !email.isBlank() ? email : sessionEmail;
            if (effectiveEmail != null && effectiveEmail.equalsIgnoreCase(ownerEmail)) {
                inquiryRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("message", "Ticket deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Unauthorized to delete this ticket.");
            }
        });
        return "redirect:/customer-support";
    }

    @PostMapping("/customer-support/{id}/update")
    public String updateInquiry(@PathVariable("id") Long id,
                                @RequestParam("email") String email,
                                @RequestParam("subject") String subject,
                                @RequestParam("message") String message,
                                jakarta.servlet.http.HttpSession session,
                                RedirectAttributes redirectAttributes) {
        return inquiryRepository.findById(id).map(inquiry -> {
            String sessionEmail = (String) session.getAttribute("supportEmail");
            String effectiveEmail = email != null && !email.isBlank() ? email : sessionEmail;
            if (effectiveEmail != null && effectiveEmail.equalsIgnoreCase(inquiry.getEmail())) {
                // Only allow editing while OPEN
                if (inquiry.getStatus() == InquiryStatus.OPEN) {
                    inquiry.setSubject(subject);
                    inquiry.setMessage(message);
                    inquiryRepository.save(inquiry);
                    redirectAttributes.addFlashAttribute("message", "Ticket updated successfully.");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Closed tickets cannot be edited.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Unauthorized to edit this ticket.");
            }
            return "redirect:/customer-support";
        }).orElse("redirect:/customer-support");
    }
}


