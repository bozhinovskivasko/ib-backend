package mk.ukim.finki.ibproekt.controller;

import mk.ukim.finki.ibproekt.service.TOTPService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api")
public class TOTPController {

    private final TOTPService totpService;

    public TOTPController(TOTPService totpService) {
        this.totpService = totpService;
    }

    @GetMapping("/totp")
    public Map<String, String> generateTOTP() {
        System.out.println("Generating TOTP...");

        return Map.of("otp", totpService.generateTOTP());
    }

    @GetMapping("/verify-totp/{totp}")
    public Map<String, String> verifyTOTP(@PathVariable String totp) {
        System.out.println("Verifying TOTP...");

        return totpService.verifyTOTP(totp);
    }
}
