package org.lox.lox;

interface LoxIndexable {

  Object get(Token token, Object index);

  void set(Token token, Object index, Object value);

  int length();

}
