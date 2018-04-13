package org.hyperion.hype;

import java.util.List;
import java.util.Map;

public class HypeClass implements HypeCallable {

  final String name;
  private final Map<String, HypeFunction> methods;

  public HypeClass(String name, Map<String, HypeFunction> methods) {
    this.name = name;
    this.methods = methods;
  }

  HypeFunction findMethod(HypeInstance instance, String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }

    return null;
  }

  @Override
  public int arity() {
    return 0;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    HypeInstance instance = new HypeInstance(this);
    return instance;
  }

  @Override
  public String toString() {
    return name;
  }

}
