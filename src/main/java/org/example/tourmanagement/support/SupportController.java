package org.example.tourmanagement.support;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
}


