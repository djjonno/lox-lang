package org.hyperion.hype;

import java.util.List;

class HypeFunction implements HypeCallable {

  private final Stmt.Function declaration;
  private final Environment closure;
  private final boolean isInitializer;

  public HypeFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
    this.declaration = declaration;
    this.closure = closure;
    this.isInitializer = isInitializer;
  }

  boolean isGetter() {
    return declaration.parameters == null;
  }

  HypeFunction bind(HypeInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    return new HypeFunction(declaration, environment, isInitializer);
  }

  @Override
  public int arity() {
    return declaration.parameters.size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment environment = new Environment(closure);
    if (declaration.parameters != null) {
      for (int i = 0; i < declaration.parameters.size(); i++) {
        environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
      }
    }

    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (ReturnJump returnValue) {
      return returnValue.value;
    }

    if (isInitializer) return closure.getAt(0, "this");
    return null;
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }
}
