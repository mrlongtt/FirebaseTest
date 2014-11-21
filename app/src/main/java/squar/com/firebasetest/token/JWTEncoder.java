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

import android.util.Base64;

import java.nio.charset.Charset;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JWTEncoder {

  private static final String TOKEN_SEP = ".";

  private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

  private static final String HMAC_256 = "HmacSHA256";

  /**
   * Encode and sign a set of claims.
   */
  public static String encode(JSONObject claims, String secret) {
    String encodedHeader = getCommonHeader();
    String encodedClaims = encodeJson(claims);

    String secureBits = new StringBuilder(encodedHeader).append(TOKEN_SEP).append(encodedClaims).toString();

    String sig = sign(secret, secureBits);

    return new StringBuilder(secureBits).append(TOKEN_SEP).append(sig).toString();
  }

  private static String sign(String secret, String secureBits) {
    try {
      Mac sha256_HMAC = Mac.getInstance(HMAC_256);
      SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(UTF8_CHARSET), HMAC_256);
      sha256_HMAC.init(secret_key);
      byte sig[] = sha256_HMAC.doFinal(secureBits.getBytes(UTF8_CHARSET));
      //      return Base64.encodeBase64URLSafeString(sig);
      return Base64.encodeToString(sig, Base64.URL_SAFE);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String getCommonHeader() {
    try {
      JSONObject headerJson = new JSONObject();
      headerJson.put("typ", "JWT");
      headerJson.put("alg", "HS256");
      return encodeJson(headerJson);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private static String encodeJson(JSONObject jsonData) {
    //    return Base64.encodeBase64URLSafeString(jsonData.toString().getBytes(UTF8_CHARSET));
    //    return new String(Base64.encodeBase64(jsonData.toString().getBytes(UTF8_CHARSET)));
    return Base64.encodeToString(jsonData.toString().getBytes(UTF8_CHARSET), Base64.URL_SAFE);
  }
}