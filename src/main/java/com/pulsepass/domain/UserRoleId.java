package com.pulsepass.domain;

import com.pulsepass.domain.enums.UserRoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserRoleId implements Serializable {
    @Column(name = "user_id") private Long userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20) private UserRoleName role;
    protected UserRoleId() {}
    public UserRoleId(Long userId, UserRoleName role) { this.userId = userId; this.role = role; }
    public Long getUserId() { return userId; }
    public UserRoleName getRole() { return role; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof UserRoleId that)) return false; return Objects.equals(userId, that.userId) && role == that.role; }
    @Override public int hashCode() { return Objects.hash(userId, role); }
}
