package com.example.soa.users;

import com.example.soa.core.DomainException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InMemoryUsersService implements UsersService {
    private final Set<UUID> users = new HashSet<>();

    public void addUser(UUID userId) {
        users.add(userId);
    }

    @Override
    public void assertUserExists(UUID userId) {
        if (!users.contains(userId)) {
            throw new DomainException("User not found: " + userId);
        }
    }
}
