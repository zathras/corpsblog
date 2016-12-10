package com.jovial.google

import com.jovial.util.notNull
import java.util.*

/**
 * Created by w.foote on 12/9/2016.
 */
class OAuthToken (jsonValue : Any){

    public var access_token : String
    public var token_type : String
    public var expires : Date
    public val refresh_token : String

    init {
        val m = jsonValue as HashMap<Any, Any>
        access_token = notNull(m, "access_token")
        token_type = notNull(m, "token_type")
        val e = m["expires"]
        if (e == null) {
            // It's a response from Google
            val expires_in = (m["expires_in"] as Number).toLong()
            expires = Date(System.currentTimeMillis() + 1000 * expires_in)
        } else {
            // It's being read from our DB
            expires = Date((e as Number).toLong())
        }
        refresh_token = notNull(m, "refresh_token")
    }

    fun asJsonValue() : HashMap<Any, Any> {
        val result = HashMap<Any, Any>()
        result["access_token"] = access_token
        result["token_type"] = token_type
        result["expires"] = expires.getTime()
        return result
    }

    fun refreshToken(jsonValue : Any) {
        val m = jsonValue as HashMap<Any, Any>
        access_token = notNull(m, "access_token")
        token_type = notNull(m, "token_type")
        val expires_in = (m["expires_in"] as Number).toLong()
        expires = Date(System.currentTimeMillis() + 1000 * expires_in)
    }
}