package org.project.service;

public interface TokenStatusChecker {
    boolean  isTokenInvalidated(String tokenHash);
    boolean  isTokenBlacklisted(String tokenHash);
}
