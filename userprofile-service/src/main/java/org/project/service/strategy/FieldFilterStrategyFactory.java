package org.project.service.strategy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FieldFilterStrategyFactory {
    List<FieldFilterStrategy> strategies;

    public FieldFilterStrategy getStrategy(Set<String> roles) {

        return strategies.stream()
                .filter(strategy -> strategy.supports(roles))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ROLE_OPERATION,
                        "No suitable field filter strategy found for roles: " + roles));
    }
}
