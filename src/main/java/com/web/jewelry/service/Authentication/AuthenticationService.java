package com.web.jewelry.service.Authentication;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.web.jewelry.dto.request.AuthenticationRequest;
import com.web.jewelry.dto.request.CollectionRequest;
import com.web.jewelry.dto.request.IntrospectRequest;
import com.web.jewelry.dto.response.AddressResponse;
import com.web.jewelry.dto.response.AuthenticationResponse;
import com.web.jewelry.dto.response.IntrospectResponse;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.User;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    @NonFinal
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    public AuthenticationResponse authenticate (AuthenticationRequest request, String role) {
        User user = switch (role) {
            case "manager" -> userService.getManagerByUsername(request.getUsername());
            case "staff" -> userService.getStaffByUsername(request.getUsername());
            case "customer" -> userService.getCustomerByUsername(request.getUsername());
            default -> null;
        };
        if (user != null) {
            boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
            if(authenticated){
                String token = generateToken(user);
                return AuthenticationResponse.builder()
                        .token(token)
                        .authenticated(true)
                        .build();
            }
        }
        throw new ResourceNotFoundException("Invalid username or password");
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("shiny.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                .claim("scope", user.getRole())
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        }
        catch (JOSEException e){
            throw new RuntimeException(e.getMessage());
        }
    }
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken();
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);
        return  IntrospectResponse.builder()
                .valid(verified && expirationDate.after(new Date()))
                .build();

    }
}
