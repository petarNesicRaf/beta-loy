package com.beta.loyalty.service.staff;

import com.beta.loyalty.domain.Role;
import com.beta.loyalty.domain.StaffUser;
import com.beta.loyalty.domain.enums.StaffStatus;
import com.beta.loyalty.dto.staff.InviteStaffRequest;
import com.beta.loyalty.dto.staff.StaffMemberResponse;
import com.beta.loyalty.dto.staff.UpdateStaffRolesRequest;
import com.beta.loyalty.dto.staff.UpdateStaffStatusRequest;
import com.beta.loyalty.exception.ConflictException;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.repository.staff.RoleRepository;
import com.beta.loyalty.repository.staff.StaffUserRepository;
import com.beta.loyalty.repository.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffMemberService {
    private final StaffUserRepository staffUserRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public StaffMemberResponse invite(UUID tenantId, InviteStaffRequest req) {
        if (staffUserRepository.existsByEmailAndTenantId(req.email(), tenantId)) {
            throw new ConflictException("A staff member with this email already exists in your tenant");
        }

        StaffUser staff = new StaffUser();
        staff.setTenant(tenantRepository.getReferenceById(tenantId));
        staff.setEmail(req.email());
        staff.setPasswordHash(passwordEncoder.encode(req.password()));
        staff.setStatus(StaffStatus.ACTIVE);

        if (req.roles() != null && !req.roles().isEmpty()) {
            List<Role> roles = roleRepository.findAllByTenantIdAndNameIn(tenantId, req.roles());
            if (roles.size() != req.roles().size()) {
                throw new NotFoundException("One or more roles not found in your tenant");
            }
            staff.setRoles(new HashSet<>(roles));
        }

        return StaffMemberResponse.from(staffUserRepository.save(staff));
    }

    @Transactional(readOnly = true)
    public Page<StaffMemberResponse> listMembers(UUID tenantId, Pageable pageable) {
        return staffUserRepository.findAllByTenantId(tenantId, pageable)
                .map(StaffMemberResponse::from);
    }

    @Transactional(readOnly = true)
    public StaffMemberResponse getMember(UUID tenantId, UUID id) {
        return StaffMemberResponse.from(findForTenant(tenantId, id));
    }

    @Transactional
    public StaffMemberResponse updateStatus(UUID tenantId, UUID id, UpdateStaffStatusRequest req) {
        StaffUser staff = findForTenant(tenantId, id);
        staff.setStatus(req.status());
        return StaffMemberResponse.from(staff);
    }

    @Transactional
    public StaffMemberResponse updateRoles(UUID tenantId, UUID id, UpdateStaffRolesRequest req) {
        StaffUser staff = findForTenant(tenantId, id);

        List<Role> roles = roleRepository.findAllByTenantIdAndNameIn(tenantId, req.roles());
        if (roles.size() != req.roles().size()) {
            throw new NotFoundException("One or more roles not found in your tenant");
        }

        staff.setRoles(new HashSet<>(roles));
        return StaffMemberResponse.from(staff);
    }

    @Transactional
    public void deleteMember(UUID tenantId, UUID id) {
        StaffUser staff = findForTenant(tenantId, id);
        staff.setStatus(StaffStatus.DISABLED);
    }

    private StaffUser findForTenant(UUID tenantId, UUID id) {
        return staffUserRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Staff member not found"));
    }
}
