package com.example.authorizationserver.oauth.endpoint;

import com.example.authorizationserver.oauth.common.BasicAuthenticationUtil;
import com.example.authorizationserver.oauth.common.ClientCredentials;
import com.example.authorizationserver.oauth.endpoint.resource.TokenRequest;
import com.example.authorizationserver.oauth.endpoint.resource.TokenResponse;
import com.example.authorizationserver.oauth.store.AuthorizationState;
import com.example.authorizationserver.oauth.store.AuthorizationStateStore;
import com.example.authorizationserver.token.store.TokenService;
import com.example.authorizationserver.user.model.User;
import com.example.authorizationserver.user.service.UserService;
import com.nimbusds.jose.JOSEException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/token")
@RestController
public class TokenEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(TokenEndpoint.class);

  private final AuthorizationStateStore authorizationStateStore;
  private final UserService userService;
  private final TokenService tokenService;

  public TokenEndpoint(AuthorizationStateStore authorizationStateStore, UserService userService, TokenService tokenService) {
    this.authorizationStateStore = authorizationStateStore;
    this.userService = userService;
    this.tokenService = tokenService;
  }

  @PostMapping
  public TokenResponse getToken(@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
                                @ModelAttribute("token_request") TokenRequest tokenRequest,
                                BindingResult result) throws JOSEException {

    AuthorizationState authorizationState = authorizationStateStore.getState(tokenRequest.getCode());
    validateTokenRequest(authorizationState, authorizationHeader, tokenRequest);
    Optional<User> user = userService.findOneByIdentifier(UUID.fromString(authorizationState.getSubject()));
    if (user.isPresent()) {

      LOG.info("Creating token response for user {}, client id {} and scopes {}", user.get().getUsername(),
              authorizationState.getClientId(), authorizationState.getScopes());

      return new TokenResponse(createAccessToken(user.get(), authorizationState.getClientId(), authorizationState.getNonce()),
              createRefreshToken(user.get(), authorizationState.getClientId()), 3600,
              createIdToken(user.get(), authorizationState.getClientId(), authorizationState.getNonce(), authorizationState.getScopes()));
    } else {
      throw new BadCredentialsException("User not found");
    }
  }

  private void validateTokenRequest(AuthorizationState authorizationState, String authorizationHeader, TokenRequest tokenRequest) {

    if (authorizationState != null) {

      // Check if code is already expired
      if (LocalDateTime.now().isAfter(authorizationState.getExpiry())) {
        // throw error
      }

      if (authorizationHeader != null) {
        ClientCredentials clientCredentials = BasicAuthenticationUtil.fromBasicAuthHeader(authorizationHeader);
        if (!authorizationState.getClientId().equals(clientCredentials.getClientId())) {
          // throw error
        }
      } else if (tokenRequest.getClient_id() != null && !tokenRequest.getClient_id().trim().isEmpty()) {
        if (!authorizationState.getClientId().equals(tokenRequest.getClient_id())) {
          // throw error
        }
      } else {
        throw new BadCredentialsException("Unauthorized");
      }
    }
  }

  private String createIdToken(User user, String clientId, String nonce, List<String> scopes) throws JOSEException {
    return tokenService.createIdToken(user, clientId, nonce, scopes).getValue();
  }

  private String createAccessToken(User user, String clientId, String nonce) throws JOSEException {
    return tokenService.createJwtAccessToken(user, clientId, nonce).getValue();
  }

  private String createRefreshToken(User user, String clientId) {
    return tokenService.createRefreshToken(user, clientId).getValue();
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<String> handle(MissingServletRequestParameterException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<String> handle(BadCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
  }

  @ExceptionHandler(JOSEException.class)
  public ResponseEntity<String> handle(JOSEException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
  }

}