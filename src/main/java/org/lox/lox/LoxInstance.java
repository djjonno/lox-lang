package org.lox.lox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {

  protected LoxClass klass;
  protected final Map<String, Object> fields = new HashMap<>();

  LoxInstance() {}

  LoxInstance(LoxClass klass) {
    this.klass = klass;
  }

  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    LoxFunction method = klass.findMethod(this, name.lexeme);
    if (method != null) return method;

    throw new RuntimeError(name,
        "Undefined property '" + name.lexeme + "'.");
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    return "<instance class " + klass.name + ">";
  }

}
