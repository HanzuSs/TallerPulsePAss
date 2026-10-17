package com.pulsepass.security;

import com.pulsepass.domain.UserCredential;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRoleRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserCredentialRepository credentialRepository;
    private final UserRoleRepository roleRepository;
    public DatabaseUserDetailsService(UserCredentialRepository credentialRepository, UserRoleRepository roleRepository) {
        this.credentialRepository = credentialRepository; this.roleRepository = roleRepository;
    }
    @Override @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Long id; try { id = Long.valueOf(userId); } catch (NumberFormatException e) { throw new UsernameNotFoundException("User not found"); }
        UserCredential credential = credentialRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!credential.getUser().isActive()) throw new UsernameNotFoundException("User inactive");
        List<SimpleGrantedAuthority> authorities = roleRepository.findByUser_Id(id).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole().name())).toList();
        return org.springframework.security.core.userdetails.User.withUsername(id.toString())
                .password(credential.getPasswordHash()).authorities(authorities).disabled(false).build();
    }
}
