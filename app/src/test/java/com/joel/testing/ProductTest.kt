package com.joel.testing

import org.junit.Test
import org.junit.jupiter.api.Assertions.*

 class ProductTest {

  private val spaghetti = Product(
   title = "Spaghetti",
   price = 20.00,
   amount = 3
  )

  private val steak = Product(
   title = "Steak",
   price = 30.00,
   amount = 8
  )

  private val lasagna = Product(
   title = "Lasagna",
   price = 10.00,
   amount = 0
  )

  @Test
  fun test1(){

   spaghetti.applyDiscount(20)

   assertEquals(
    16.00,
    spaghetti.price,
    0.0
   )

  }

  @Test
  fun test2(){

   steak.applyDiscount(20)

   assertEquals(
    30.00,
    steak.price,
    0.0
   )

  }

  @Test
  fun test3(){

   lasagna.applyDiscount(20)

   assertEquals(
    10.00,
    lasagna.price,
    0.0
   )

  }

 }