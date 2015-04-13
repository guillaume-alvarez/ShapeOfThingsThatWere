package com.galvarez.ttw.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class RomanNumbers {

  private RomanNumbers() {
  }

  private static Map<String, Integer> ROMAN_NUMERALS = new LinkedHashMap<String, Integer>();
  static {
    ROMAN_NUMERALS.put("M", 1000);
    ROMAN_NUMERALS.put("CM", 900);
    ROMAN_NUMERALS.put("D", 500);
    ROMAN_NUMERALS.put("CD", 400);
    ROMAN_NUMERALS.put("C", 100);
    ROMAN_NUMERALS.put("XC", 90);
    ROMAN_NUMERALS.put("L", 50);
    ROMAN_NUMERALS.put("XL", 40);
    ROMAN_NUMERALS.put("X", 10);
    ROMAN_NUMERALS.put("IX", 9);
    ROMAN_NUMERALS.put("V", 5);
    ROMAN_NUMERALS.put("IV", 4);
    ROMAN_NUMERALS.put("I", 1);
  }

  public static String toRoman(int integer) {
    StringBuilder res = new StringBuilder(integer / 5);
    for (Entry<String, Integer> entry : ROMAN_NUMERALS.entrySet()) {
      int value = entry.getValue();
      int matches = integer / value;
      for (int i = 0; i < matches; i++)
        res.append(entry.getKey());
      integer = integer % value;
    }
    return res.toString();
  }

}
