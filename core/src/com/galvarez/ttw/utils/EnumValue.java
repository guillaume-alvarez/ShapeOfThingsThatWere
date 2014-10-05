package com.galvarez.ttw.utils;

public final class EnumValue<E extends Enum<E>> {

  private final E defaultValue;

  private final Class<E> enumClass;

  private E value;

  @SuppressWarnings("unchecked")
  public EnumValue(E defaultValue) {
    this.defaultValue = defaultValue;
    this.enumClass = (Class<E>) defaultValue.getClass();
    this.value = defaultValue;
  }

  public void set(E value) {
    this.value = value != null ? value : defaultValue;
  }

  public void set(String text) {
    try {
      if (text != null && !text.isEmpty())
        value = Enum.valueOf(enumClass, text);
      else
        value = defaultValue;
    } catch (Exception e) {
      value = defaultValue;
    }
  }

  public E get() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
