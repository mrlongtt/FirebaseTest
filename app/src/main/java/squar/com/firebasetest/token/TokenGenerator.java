/*
 * Copyright (C) 2014 MySQUAR. All rights reserved.
 *
 * This software is the confidential and proprietary information of MySQUAR or one of its
 * subsidiaries. You shall not disclose this confidential information and shall use it only in
 * accordance with the terms of the license agreement or other applicable agreement you entered into
 * with MySQUAR.
 *
 * MySQUAR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. MySQUAR SHALL NOT BE LIABLE FOR ANY LOSSES
 * OR DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */

package squar.com.firebasetest.token;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

public class TokenGenerator {

  private static final int TOKEN_VERSION = 0;

  private final String firebaseSecret;

  /**
   * Default constructor given a Firebase secret.
   */
  public TokenGenerator(String firebaseSecret) {
    super();
    this.firebaseSecret = firebaseSecret;
  }

  /**
   * Create a token for the given object.
   */
  public String createToken(Map<String, Object> data) {
    return createToken(data, new TokenOptions());
  }

  /**
   * Create a token for the given object and options.
   */
  public String createToken(Map<String, Object> data, TokenOptions options) {
    if ((data == null || data.size() == 0) && (options == null || (!options.isAdmin() && !options.isDebug()))) {
      throw new IllegalArgumentException(
          "TokenGenerator.createToken: data is empty and no options are set.  This token will have no effect on Firebase.");
    }

    JSONObject claims = new JSONObject();

    try {
      claims.put("v", TOKEN_VERSION);
      claims.put("iat", new Date().getTime() / 1000);

      boolean isAdminToken = (options != null && options.isAdmin());
      validateToken("TokenGenerator.createToken", data, isAdminToken);

      if (data != null && data.size() > 0) {
        claims.put("d", new JSONObject(data));
      }

      // Handle options
      if (options != null) {
        if (options.getExpires() != null) {
          claims.put("exp", options.getExpires().getTime() / 1000);
        }

        if (options.getNotBefore() != null) {
          claims.put("nbf", options.getNotBefore().getTime() / 1000);
        }

        // Only add these claims if they're true to avoid sending them over the wire when false.
        if (options.isAdmin()) {
          claims.put("admin", options.isAdmin());
        }

        if (options.isDebug()) {
          claims.put("debug", options.isDebug());
        }
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }

    String token = computeToken(claims);
    if (token.length() > 1024) {
      throw new IllegalArgumentException(
          "TokenGenerator.createToken: Generated token is too long. The token cannot be longer than 1024 bytes.");
    }
    return token;
  }

  private String computeToken(JSONObject claims) {
    return JWTEncoder.encode(claims, firebaseSecret);
  }

  private void validateToken(String functionName, Map<String, Object> data, boolean isAdminToken) {
    boolean containsUid = (data != null && data.containsKey("uid"));
    if ((!containsUid && !isAdminToken) || (containsUid && !(data.get("uid") instanceof String))) {
      throw new IllegalArgumentException(functionName + ": Data payload must contain a \"uid\" key that must be a string.");
    } else if (containsUid && data.get("uid").toString().length() > 256) {
      throw new IllegalArgumentException(
          functionName + ": Data payload must contain a \"uid\" key that must not be longer than 256 characters.");
    }
  }
}