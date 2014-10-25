package com.galvarez.ttw.utils;

public final class BooleanValue {

  private final boolean defaultValue;

  private boolean value;

  public BooleanValue(boolean defaultValue) {
    this.defaultValue = defaultValue;
    this.value = defaultValue;
  }

  public void set(boolean value) {
    this.value = value;
  }

  public void set(String text) {
    if (text != null && !text.isEmpty())
      value = "true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text);
    else
      value = defaultValue;
  }

  public boolean get() {
    return value;
  }

  @Override
  public String toString() {
    return Boolean.toString(defaultValue);
  }
}
