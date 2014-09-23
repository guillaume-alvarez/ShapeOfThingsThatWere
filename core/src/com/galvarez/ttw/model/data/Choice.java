package com.galvarez.ttw.model.data;

public enum Choice {

  FOOD("Our people prefer eating "),

  WEAPON("When fighting, our people use "),

  MOVE("We go farther and better by "),

  TERRAIN("Our preferred terrain is "),

  GOVERNMENT("We are governed by ");

  public final String msg;

  private Choice(String msg) {
    this.msg = msg;
  }

  public static final Choice get(String name) {
    try {
      return Choice.valueOf(name);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

}
