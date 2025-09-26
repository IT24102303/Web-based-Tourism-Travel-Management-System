package org.example.tourmanagement.admin;

import org.example.tourmanagement.support.Inquiry;
import org.example.tourmanagement.support.InquiryRepository;
import org.example.tourmanagement.support.InquiryStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/inquiries")
public class AdminInquiryController {

    private final InquiryRepository inquiryRepository;

    public AdminInquiryController(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    @GetMapping
    public String manageInquiries(@RequestParam(value = "status", required = false) String status, Model model) {
        List<Inquiry> inquiries;
        if (status != null && !status.isBlank()) {
            try {
                InquiryStatus st = InquiryStatus.valueOf(status.toUpperCase());
                inquiries = inquiryRepository.findByStatusOrderByCreatedAtDesc(st);
            } catch (IllegalArgumentException e) {
                inquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();
            }
        } else {
            inquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();
        }
        model.addAttribute("inquiries", inquiries);
        model.addAttribute("selectedStatus", status);
        return "manage-inquiries";
    }

    @GetMapping("/{id}")
    public String viewInquiry(@PathVariable("id") Long id, Model model) {
        Inquiry inquiry = inquiryRepository.findById(id).orElse(null);
        if (inquiry == null) {
            return "redirect:/admin/inquiries?notfound";
        }
        model.addAttribute("inquiry", inquiry);
        return "inquiry-detail";
    }

    @PostMapping("/{id}/reply")
    public String replyInquiry(@PathVariable("id") Long id, @RequestParam("replyMessage") String replyMessage) {
        inquiryRepository.findById(id).ifPresent(inquiry -> {
            inquiry.setReplyMessage(replyMessage);
            inquiry.setRepliedAt(java.time.LocalDateTime.now());
            inquiry.setStatus(org.example.tourmanagement.support.InquiryStatus.CLOSED);
            inquiryRepository.save(inquiry);
        });
        return "redirect:/admin/inquiries/" + id + "?replied";
    }

    @PostMapping("/{id}/close")
    public String closeInquiry(@PathVariable("id") Long id) {
        inquiryRepository.findById(id).ifPresent(inquiry -> {
            inquiry.setStatus(InquiryStatus.CLOSED);
            inquiryRepository.save(inquiry);
        });
        return "redirect:/admin/inquiries?updated";
    }
}


