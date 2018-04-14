package org.hyperion.hype;

import java.util.HashMap;
import java.util.Map;

public class HypeInstance {

  protected HypeClass klass;
  protected final Map<String, Object> fields = new HashMap<>();

  HypeInstance() {}

  HypeInstance(HypeClass klass) {
    this.klass = klass;
  }

  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    HypeFunction method = klass.findMethod(this, name.lexeme);
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
