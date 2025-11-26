package org.project.service.impl;

import org.project.service.UrlProcessor;
import org.springframework.stereotype.Component;

@Component
public class PresignedUrlProcessor implements UrlProcessor {

    @Override
    public String extractFileName(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        String cleanUrl = url.split("\\?")[0];

        int lastSlashIndex = cleanUrl.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < cleanUrl.length() - 1) {
            return cleanUrl.substring(lastSlashIndex + 1);
        }

        return cleanUrl;
    }
}
