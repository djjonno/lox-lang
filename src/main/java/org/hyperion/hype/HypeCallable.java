package org.hyperion.hype;

import java.util.List;

interface HypeCallable {
  int arity();
  Object call(Interpreter interpreter, List<Object> arguments);
}
