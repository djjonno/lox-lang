package org.hyperion.hype;

import java.util.List;
import java.util.Map;

public class HypeClass extends HypeInstance implements HypeCallable {

  final String name;
  private final Map<String, HypeFunction> methods;

  public HypeClass(String name, Map<String, HypeFunction> methods) {
    this.name = name;
    this.methods = methods;
    this.klass = this;
  }

  HypeFunction findMethod(HypeInstance instance, String name) {
    if (methods.containsKey(name)) {
      return methods.get(name).bind(instance);
    }

    return null;
  }

  @Override
  public int arity() {
    HypeFunction initializer = methods.get("init");
    if (initializer == null) return 0;
    return initializer.arity();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    HypeInstance instance = new HypeInstance(this);
    HypeFunction initializer = methods.get("init");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }
    return instance;
  }

  @Override
  public String toString() {
    return name;
  }

}
