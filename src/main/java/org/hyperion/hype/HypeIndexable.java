package org.hyperion.hype;

interface HypeIndexable {

  Object get(Token token, Object index);

  void set(Token token, Object index, Object value);

  int length();

}
