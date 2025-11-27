package com.example.soa.users;

import java.util.UUID;

public interface UsersService {
    void assertUserExists(UUID userId);
}
