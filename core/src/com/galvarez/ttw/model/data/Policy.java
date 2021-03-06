package com.galvarez.ttw.model.data;

public enum Policy {

  FOOD("Our people prefer eating "),

  WEAPON("When fighting, our people use "),

  MOVE("We go farther and better by "),

  TERRAIN("Our preferred terrain is "),

  RELATION("Our preferred relation is "),

  VALUE("Our most honored value is "),

  ART("Our most excellent art is "),

  GOVERNMENT("We are governed by "),

  JOBS("Our people are mostly ");

  public final String msg;

  private Policy(String msg) {
    this.msg = msg;
  }

  public static final Policy get(String name) {
    try {
      return Policy.valueOf(name);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

}
