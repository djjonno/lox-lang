package org.hyperion.hype;

import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class NativeFunctions {
  public static void define(Environment env) {
    env.define("clock", clock);
    env.define("log", log);
    env.define("input", input);
  }

  private final static HypeCallable clock = new HypeCallable() {
    @Override
    public int arity() {
      return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
      return (double)System.currentTimeMillis() / 1000.0;
    }
  };

  private final static HypeCallable log = new HypeCallable() {
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
      StringJoiner j = new StringJoiner(", ");
      for (Object o : arguments)
        j.add(interpreter.stringify(o));
      System.out.println(j.toString());
      return null;
    }
  };

  private final static HypeCallable input = new HypeCallable() {
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
      System.out.print(arguments.stream()
        .map(interpreter::stringify)
        .collect(Collectors.joining(" ")));
      Scanner sc = new Scanner(System.in);
      return sc.nextLine();
    }
  };

}
