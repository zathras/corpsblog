package com.jovial.oauth

import com.jovial.util.notNull
import com.jovial.util.nullOK
import java.util.*

/**
 * Created by w.foote on 12/9/2016.
 */
class OAuthToken (jsonValue : Any){

    public var access_token : String
    public var token_type : String
    public var expires : Date
    public val refresh_token : String?

    init {
        val m = jsonValue as HashMap<Any, Any>
        access_token = notNull(m, "access_token")
        token_type = nullOK(m, "token_type") ?: "OAuth"     // Mailchimp doesn't give token_type
        val e = m["expires"]
        if (e == null) {
            // It's a response from the oauth server
            val expires_in = (m["expires_in"] as Number).toLong()
            if (expires_in == 0L) {
                // Mailchimp tokens don't expire, and their expires_in value is set to zero.
                expires = Date(10000L * 365 * 24 * 60 * 60 * 1000)  // 10,000 years after 1/1/1970
            } else {
                expires = Date(System.currentTimeMillis() + 1000 * expires_in)
            }
        } else {
            // It's being read from our DB
            expires = Date((e as Number).toLong())
        }
        refresh_token = nullOK(m, "refresh_token")      // Not used or provided for Mailchimp
    }

    fun toJsonValue() : HashMap<Any, Any> {
        val result = HashMap<Any, Any>()
        result["access_token"] = access_token
        result["token_type"] = token_type
        result["expires"] = expires.getTime()
        if (refresh_token != null) {
            result["refresh_token"] = refresh_token
        }
        return result
    }

    /**
     * Refresh this token from a JSON response from the server
     */
    fun refreshToken(jsonValue : Any) {
        val m = jsonValue as HashMap<Any, Any>
        access_token = notNull(m, "access_token")
        token_type = notNull(m, "token_type")
        val expires_in = (m["expires_in"] as Number).toLong()
        expires = Date(System.currentTimeMillis() + 1000 * expires_in)
    }
}