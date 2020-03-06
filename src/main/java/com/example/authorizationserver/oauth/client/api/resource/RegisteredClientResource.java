package com.example.authorizationserver.oauth.client.api.resource;

import com.example.authorizationserver.oauth.client.model.AccessTokenFormat;
import com.example.authorizationserver.oauth.client.model.RegisteredClient;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RegisteredClientResource {

  @NotNull private UUID identifier;

  @NotBlank
  @Size(max = 100)
  private String clientId;

  @Size(max = 100)
  private String clientSecret;

  @NotNull private boolean confidential;

  @NotNull private boolean offline;

  @NotNull private boolean directGrant;

  @NotNull
  @Enumerated(EnumType.STRING)
  private AccessTokenFormat accessTokenFormat;

  @NotEmpty private Set<String> redirectUris = new HashSet<>();

  @NotEmpty private Set<String> corsUris = new HashSet<>();

  public RegisteredClientResource() {}

  public RegisteredClientResource(RegisteredClient registeredClient) {
    this.identifier = registeredClient.getIdentifier();
    this.accessTokenFormat = registeredClient.getAccessTokenFormat();
    this.clientId = registeredClient.getClientId();
    this.clientSecret = registeredClient.getClientSecret();
    this.confidential = registeredClient.isConfidential();
    this.corsUris = registeredClient.getCorsUris();
    this.directGrant = registeredClient.isDirectGrant();
    this.offline = registeredClient.isOffline();
    this.redirectUris = registeredClient.getRedirectUris();
  }

  public UUID getIdentifier() {
    return identifier;
  }

  public void setIdentifier(UUID identifier) {
    this.identifier = identifier;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public boolean isConfidential() {
    return confidential;
  }

  public void setConfidential(boolean confidential) {
    this.confidential = confidential;
  }

  public boolean isOffline() {
    return offline;
  }

  public void setOffline(boolean offline) {
    this.offline = offline;
  }

  public boolean isDirectGrant() {
    return directGrant;
  }

  public void setDirectGrant(boolean directGrant) {
    this.directGrant = directGrant;
  }

  public Set<String> getRedirectUris() {
    return redirectUris;
  }

  public void setRedirectUris(Set<String> redirectUris) {
    this.redirectUris = redirectUris;
  }

  public Set<String> getCorsUris() {
    return corsUris;
  }

  public void setCorsUris(Set<String> corsUris) {
    this.corsUris = corsUris;
  }

  public AccessTokenFormat getAccessTokenFormat() {
    return accessTokenFormat;
  }

  public void setAccessTokenFormat(AccessTokenFormat accessTokenFormat) {
    this.accessTokenFormat = accessTokenFormat;
  }

  @Override
  public String toString() {
    return "RegisteredClient{"
        + "clientId='"
        + clientId
        + '\''
        + ", clientSecret='*****'"
        + ", confidential="
        + confidential
        + ", accessTokenFormat="
        + accessTokenFormat
        + ", offline="
        + offline
        + ", directGrant="
        + directGrant
        + ", redirectUris="
        + redirectUris
        + ", corsUris="
        + corsUris
        + '}';
  }
}