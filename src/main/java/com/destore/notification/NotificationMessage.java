package com.destore.notification;

import java.time.Instant;

public record NotificationMessage(
        String type,
        String detail,
        Instant createdAt
) {
}
