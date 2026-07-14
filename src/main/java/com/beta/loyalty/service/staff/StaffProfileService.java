package com.beta.loyalty.service.staff;

import com.beta.loyalty.dto.staff.ChangePasswordRequest;
import com.beta.loyalty.dto.staff.StaffProfileResponse;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.exception.UnauthorizedException;
import com.beta.loyalty.repository.staff.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffProfileService {

    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public StaffProfileResponse getProfile(UUID staffUserId) {
        return staffUserRepository.findById(staffUserId)
                .map(StaffProfileResponse::from)
                .orElseThrow(() -> new NotFoundException("Staff not found"));
    }

    @Transactional
    public void changePassword(UUID staffUserId, ChangePasswordRequest req) {
        var staff = staffUserRepository.findById(staffUserId)
                .orElseThrow(() -> new NotFoundException("Staff not found"));
        if (!passwordEncoder.matches(req.currentPassword(), staff.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        staff.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        staffUserRepository.save(staff);
    }
}
