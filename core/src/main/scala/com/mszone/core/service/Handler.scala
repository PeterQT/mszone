package com.mszone.core.service

trait Handler[I,O] {
  def handle(input: I) : O
}
