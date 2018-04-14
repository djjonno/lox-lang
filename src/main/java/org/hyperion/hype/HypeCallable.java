package org.hyperion.hype;

import java.util.List;

interface HypeCallable {
  int arity();
  default boolean variadic() {
    return false;
  }
  Object call(Interpreter interpreter, List<Object> arguments);
}
