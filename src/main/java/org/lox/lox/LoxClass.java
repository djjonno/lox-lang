package org.lox.lox;

import java.util.List;
import java.util.Map;

public class LoxClass extends LoxInstance implements LoxCallable {

  final String name;
  final LoxClass superclass;
  private final Map<String, LoxFunction> methods;

  public LoxClass(LoxClass metaClass,
                  LoxClass superclass,
                  String name,
                  Map<String, LoxFunction> methods) {
    super(metaClass);
    this.superclass = superclass;
    this.name = name;
    this.methods = methods;
  }

  LoxFunction findMethod(LoxInstance instance, String name) {
    if (methods.containsKey(name)) {
      return methods.get(name).bind(instance);
    }

    if (superclass != null) {
      return superclass.findMethod(instance, name);
    }

    return null;
  }

  @Override
  public int arity() {
    LoxFunction initializer = methods.get("init");
    if (initializer == null) return 0;
    return initializer.arity();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    LoxInstance instance = new LoxInstance(this);
    LoxFunction initializer = methods.get("init");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }
    return instance;
  }

  @Override
  public String toString() {
    return "<" + name + ">";
  }

}
