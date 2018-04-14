package org.hyperion.hype;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HypeArray implements HypeIndexable {

  private List<Object> elements;
  private final Map<String, HypeCallable> methods;

  HypeArray(List<Object> elements) {
    this.elements = elements;
    methods = createMethods(this);
  }

  private static Map<String, HypeCallable> createMethods(HypeArray array) {
    Map<String, HypeCallable> methods = new HashMap<>();

    methods.put("add", new HypeCallable() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public boolean variadic() {
        return true;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return array.elements.addAll(arguments);
      }
    });
    methods.put("pop", new HypeCallable() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        try {
          array.elements.remove(0);
        } catch (IndexOutOfBoundsException e) {
          return null;
        }
        return null;
      }
    });
    methods.put("remove", new HypeCallable() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        try {
          int idx = ((Double) arguments.get(0)).intValue();
          return array.elements.remove(idx);
        } catch (NumberFormatException e) {
          throw new RuntimeError(null, "Array index must be numeric.");
        } catch (IndexOutOfBoundsException e) {
          throw new RuntimeError(null, "Array index out of bounds.");
        }
      }
    });
    methods.put("length", new HypeCallable() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return new Double(array.length());
      }
    });
    methods.put("isEmpty", new HypeCallable() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return array.elements.isEmpty();
      }
    });

    return methods;
  }

  @Override
  public Object get(Token token, Object index) {
    int i = indexToInteger(token, index);
    try {
      return elements.get(i);
    } catch (IndexOutOfBoundsException e) {
      throw new RuntimeError(token, "Array index out of bounds.");
    }
  }

  @Override
  public void set(Token token, Object index, Object value) {
    int i = indexToInteger(token, index);
    try {
      elements.set(i, value);
    } catch (IndexOutOfBoundsException e) {
      throw new RuntimeError(token, "Array index out of bounds.");
    }
  }

  @Override
  public int length() {
    return elements.size();
  }

  public List<Object> getElements() {
    return elements;
  }

  public Object getMethod(Token name) {
    if (methods.containsKey(name.lexeme)) {
      return methods.get(name.lexeme);
    }
    throw new RuntimeError(name, "Undefined method.");
  }

  /**
   * Helper method to convert index (double) to int.
   *
   * @param token
   * @param index
   * @return
   */
  private int indexToInteger(Token token, Object index) {
    if (index instanceof Double) {
      double idx = ((Double) index).doubleValue();
      // All number literals in Lox are doubles, have to do a little hack
      if (idx == Math.floor(idx)) {
        // Allow negative indexing like Python
        return (idx < 0) ? Math.floorMod((int)idx, elements.size()) : (int)idx;
      }
    }
    throw new RuntimeError(token, "Array index must be an integer.");
  }
}
