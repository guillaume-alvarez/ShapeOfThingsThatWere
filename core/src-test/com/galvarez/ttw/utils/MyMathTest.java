package com.galvarez.ttw.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MyMathTest {

  @Test
  void min_two_args() {
    assertEquals(1, MyMath.min(1, 2));
    assertEquals(2, MyMath.min(3, 2));
    assertEquals(5, MyMath.min(5, 5));
  }

  @Test
  void min_three_args() {
    assertEquals(1, MyMath.min(3, 1, 2));
    assertEquals(1, MyMath.min(1, 3, 2));
    assertEquals(1, MyMath.min(2, 3, 1));
    assertEquals(5, MyMath.min(5, 5, 5));
  }

  @Test
  void max_two_args() {
    assertEquals(2, MyMath.max(1, 2));
    assertEquals(3, MyMath.max(3, 2));
    assertEquals(5, MyMath.max(5, 5));
  }

  @Test
  void pow_positive_exponent() {
    assertEquals(1, MyMath.pow(1, 5));
    assertEquals(8, MyMath.pow(2, 3));
    assertEquals(27, MyMath.pow(3, 3));
    assertEquals(100, MyMath.pow(10, 2));
  }

  @Test
  void pow_exponent_one() {
    assertEquals(7, MyMath.pow(7, 1));
  }
}
