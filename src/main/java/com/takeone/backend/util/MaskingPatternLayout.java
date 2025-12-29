package com.takeone.backend.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom logback layout to mask PII data like emails and phone numbers.
 */
public class MaskingPatternLayout extends PatternLayout {

    // Pattern for Email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([\\w\\.\\-]+)@([\\w\\-]+)(\\.[\\w\\-]+)+");
    // Pattern for Phone numbers (basic 10-12 digit pattern)
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?\\d{10,12}");

    @Override
    public String doLayout(ILoggingEvent event) {
        String message = super.doLayout(event);
        return maskMessage(message);
    }

    private String maskMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        StringBuilder sb = new StringBuilder(message);

        // Mask Emails
        maskWithPattern(sb, EMAIL_PATTERN);

        // Mask Phone Numbers
        maskWithPattern(sb, PHONE_PATTERN);

        return sb.toString();
    }

    private void maskWithPattern(StringBuilder sb, Pattern pattern) {
        Matcher matcher = pattern.matcher(sb.toString());
        int offset = 0;

        while (matcher.find()) {
            int start = matcher.start() + offset;
            int end = matcher.end() + offset;

            String original = sb.substring(start, end);
            String masked = performMasking(original);

            sb.replace(start, end, masked);
            offset += (masked.length() - original.length());
        }
    }

    private String performMasking(String value) {
        String mask = "******";
        if (value.contains("@")) {
            // Email masking: u****r@example.com
            int atIndex = value.indexOf("@");
            if (atIndex > 2) {
                return value.charAt(0) + mask + value.substring(atIndex - 1);
            }
        }
        return mask;
    }
}
