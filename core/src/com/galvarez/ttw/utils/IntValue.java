package com.galvarez.ttw.utils;

public final class IntValue {

  private final int defaultValue;

  private int value;

  public IntValue(int defaultValue) {
    this.defaultValue = defaultValue;
    this.value = defaultValue;
  }

  public void set(String text) {
    try {
      if (text != null && !text.isEmpty())
        value = Integer.parseInt(text);
      else
        value = defaultValue;
    } catch (Exception e) {
      value = defaultValue;
    }
  }

  public int get() {
    return value;
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }
}
