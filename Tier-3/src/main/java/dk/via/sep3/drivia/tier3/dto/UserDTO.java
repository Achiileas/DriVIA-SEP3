package dk.via.sep3.drivia.tier3.dto;

import dk.via.sep3.drivia.tier3.entity.UserRole;

// No password here, so it never leaves Tier 3
public record UserDTO( int id, String name, String email, UserRole role ){
}
