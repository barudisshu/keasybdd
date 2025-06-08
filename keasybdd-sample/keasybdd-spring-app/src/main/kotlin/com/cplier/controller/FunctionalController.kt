package com.cplier.controller

import com.cplier.component.ExchangeComponent
import com.cplier.model.RequestVo
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
public class FunctionalController(
  private val exchangeComponent: ExchangeComponent,
) {
  internal companion object {
    private val log = LoggerFactory.getLogger(FunctionalController::class.java)
  }

  @PostMapping("/exchange")
  public fun exchangeRequest(
    @RequestBody request: RequestVo,
  ): ResponseEntity<String> {
    log.debug("exchange...")
    return exchangeComponent.proxy(request)
  }
}
