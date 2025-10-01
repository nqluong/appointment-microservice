package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.VerifyTokenRequest;
import org.project.dto.response.TokenInfo;
import org.project.dto.response.VerifyTokenResponse;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.security.jwt.validator.TokenValidator;
import org.project.service.TokenInfoExactor;
import org.project.service.TokenStatusChecker;
import org.project.service.TokenVerificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StandardTokenVerificationService implements TokenVerificationService {

    TokenValidator tokenValidator;
    TokenInfoExactor tokenInfoExtractor;
    TokenStatusChecker tokenStatusChecker;

    @Override
    public VerifyTokenResponse verifyToken(VerifyTokenRequest request) {
       try {
            String token = request.getToken();
            if(!isTokenFormatValid(token)){
                throw new CustomException(ErrorCode.TOKEN_INVALID);
            }
            TokenInfo tokenInfo = extractTokenInformation(token);

            if(tokenInfo.isExpired()){
                throw new CustomException(ErrorCode.TOKEN_EXPIRED);
            }

           String tokenHash = tokenValidator.hash(token);
           if (isTokenBlacklisted(tokenHash)) {
               return VerifyTokenResponse.invalid("Token has been invalidated");
           }
           return VerifyTokenResponse.valid(tokenInfo);

       }catch (CustomException e){
           return VerifyTokenResponse.invalid(e.getMessage());
       }catch (Exception e) {

           return VerifyTokenResponse.invalid("Token verification failed due to internal error");
       }

    }

    private boolean isTokenFormatValid(String token) {
        return tokenValidator.validate(token);
    }

    private TokenInfo extractTokenInformation(String token) {
        return tokenInfoExtractor.extractTokenInfo(token);
    }

    private boolean isTokenBlacklisted(String tokenHash) {
        return tokenStatusChecker.isTokenInvalidated(tokenHash);
    }
}
