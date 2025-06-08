package com.cplier.model

import com.fasterxml.jackson.annotation.JsonCreator

public data class RequestVo
  @JsonCreator
  constructor(
    val secret: String,
  )
