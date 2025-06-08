package com.cplier.component

import com.cplier.model.RequestVo
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
public open class ExchangeComponent(
  private val objectMapper: ObjectMapper,
  private val okHttpClient: OkHttpClient,
) {
  @Value("\${spring.proxy.url}")
  private lateinit var url: String

  // diliver to proxy url service.
  public fun proxy(request: RequestVo): ResponseEntity<String> {
    val body = objectMapper.writeValueAsString(request)
    val rq: Request =
      Request
        .Builder()
        .url(url.toHttpUrl())
        .method("POST", body.toRequestBody(contentType = "application/json; charset=utf-8".toMediaTypeOrNull()))
        .build()
    okHttpClient.newCall(rq).execute().use {
      val response = it.body.string()
      return ResponseEntity.status(200).body(response)
    }
  }
}
