package com.web.jewelry.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class OrderIdGenerator {
    private static final String PREFIX = "SNY";
    private static final int RANDOM_LENGTH = 6;
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final Random random = new Random();

    private OrderIdGenerator() {
    }

    private static class Holder {
        private static final OrderIdGenerator INSTANCE = new OrderIdGenerator();
    }

    public static OrderIdGenerator getInstance() {
        return Holder.INSTANCE;
    }

    public String generateCode(LocalDateTime date) {
        DateTimeFormatter formatter
                = DateTimeFormatter.ofPattern(
                "yyMMddHHmm");
        String timeStamp = date.format(formatter);

        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            randomPart.append(CHAR_POOL.charAt(index));
        }

        return PREFIX + timeStamp + randomPart;
    }
}
