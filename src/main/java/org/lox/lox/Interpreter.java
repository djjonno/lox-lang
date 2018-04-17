package org.lox.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.lox.lox.TokenType.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

  final Environment globals = new Environment();
  private Environment environment = globals;
  private final Map<Expr, Integer> locals = new HashMap<>();

  public Interpreter() {
    NativeFunctions.define(globals);
  }

  void interpret(List<Stmt> statements) {
    try {
      for (Stmt stmt : statements) {
        execute(stmt);
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;
    try {
      this.environment = environment;
      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      this.environment = previous;
    }
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.body);
    }
    return null;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    environment.define(stmt.name.lexeme, null);

    Object superclass = null;
    if (stmt.superclass != null) {
      superclass = evaluate(stmt.superclass);
      if (!(superclass instanceof LoxClass)) {
        throw new RuntimeError(stmt.name, "Superclass must be a class");
      }

      environment = new Environment(environment);
      environment.define("super", superclass);
    }

    Map<String, LoxFunction> classMethods = new HashMap<>(),
      methods = new HashMap<>();

    for (Stmt.Function staticMethod : stmt.classMethods) {
      LoxFunction function = new LoxFunction(staticMethod, environment, false);
      classMethods.put(staticMethod.name.lexeme, function);
    }

    LoxClass metaClass = new LoxClass(null, ((LoxClass) superclass),
        stmt.name.lexeme + " metaClass", classMethods);

    for (Stmt.Function method : stmt.methods) {
      LoxFunction function = new LoxFunction(method, environment,
          method.name.lexeme.equals("init"));
      methods.put(method.name.lexeme, function);
    }

    LoxClass klass = new LoxClass(metaClass, ((LoxClass) superclass), stmt.name.lexeme, methods);
    if (superclass != null) {
      environment = environment.enclosing;
    }

    environment.assign(stmt.name, klass);
    return null;
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case GREATER:
        checkNumberOperands(expr.operator, left, right);
        return (double)left > (double)right;
      case GREATER_EQUAL:
        checkNumberOperands(expr.operator, left, right);
        return (double)left >= (double)right;
      case LESS:
        checkNumberOperands(expr.operator, left, right);
        return (double)left < (double)right;
      case LESS_EQUAL:
        checkNumberOperands(expr.operator, left, right);
        return (double)left <= (double)right;
      case MINUS:
        checkNumberOperand(expr.operator, right);
        return (double)left - (double)right;
      case PLUS:
        if (left instanceof Double && right instanceof Double) {
          return (double)left + (double)right;
        }

        if (left instanceof String && right instanceof String) {
          return (String)left + (String)right;
        }

        if (left instanceof String && right instanceof Double) {
          return (String)left + stringify(right);
        }

        if (left instanceof Double && right instanceof String) {
          return stringify(left) + (String)right;
        }

        throw new RuntimeError(expr.operator,
                "Operands must be two numbers or two strings.");
      case SLASH:
        checkNumberOperands(expr.operator, left, right);
        if ((Double)right == 0.0) throw new RuntimeError(expr.operator, "division by zero.");
        return (double)left / (double)right;
      case STAR:
        checkNumberOperands(expr.operator, left, right);
        return (double)left * (double)right;
      case EXPONENT:
        checkNumberOperands(expr.operator, left, right);
        return Math.pow((double)left, (double)right);
      case MODULO:
        checkNumberOperands(expr.operator, left, right);
        return (double)left % (double)right;

      case BANG_EQUAL: return !isEqual(left, right);
      case EQUAL_EQUAL: return isEqual(left, right);
      case COMMA:
        // discard the left, continue with the right.
        return right;
    }

    // unreachable
    return null;
  }

  @Override
  public Object visitCallExpr(Expr.Call expr) {
    Object callee = evaluate(expr.callee);
    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments) {
      arguments.add(evaluate(argument));
    }

    if (!(callee instanceof LoxCallable)) {
      throw new RuntimeError(expr.paren,
              "Can only call functions and classes.");
    }

    LoxCallable function = (LoxCallable)callee;

    if (arguments.size() != function.arity() && !function.variadic()) {
      throw new RuntimeError(expr.paren, "Expected " +
              function.arity() + " arguments but got " +
              arguments.size() + ".");
    }

    return function.call(this, arguments);
  }

  @Override
  public Object visitGetExpr(Expr.Get expr) {
    Object object = evaluate(expr.object);

    if (object instanceof LoxInstance) {
      Object result = ((LoxInstance) object).get(expr.name);
      if (result instanceof LoxFunction && ((LoxFunction) result).isGetter()) {
        result = ((LoxFunction) result).call(this, null);
      }
      return result;
    }

    if (object instanceof LoxArray) {
      return ((LoxArray) object).getMethod(expr.name);
    }

    throw new RuntimeError(expr.name,
        "Only instances have properties.");
  }

  @Override
  public Object visitSetExpr(Expr.Set expr) {
    Object object = evaluate(expr.object);

    if (!(object instanceof LoxInstance)) {
      throw new RuntimeError(expr.name, "Only instances have fields.");
    }

    Object value = evaluate(expr.value);
    ((LoxInstance) object).set(expr.name, value);

    return value;
  }

  @Override
  public Object visitIndexGetExpr(Expr.IndexGet expr) {
    Object indexee = evaluate(expr.indexee);
    Object index = evaluate(expr.index);
    if (indexee instanceof LoxIndexable) {
      return ((LoxIndexable) indexee).get(expr.bracket, index);
    }
    return null;
  }

  @Override
  public Object visitIndexSetExpr(Expr.IndexSet expr) {
    Object indexee = evaluate(expr.indexee);
    if (!(indexee instanceof LoxIndexable)) {
      throw new RuntimeError(expr.bracket, "Variable is not indexable");
    }
    Object index = evaluate(expr.index);
    Object value = evaluate(expr.value);
    ((LoxIndexable) indexee).set(expr.bracket, index, value);
    return value;
  }

  @Override
  public Object visitSuperExpr(Expr.Super expr) {
    int distance = locals.get(expr);
    LoxClass superclass = (LoxClass)environment.getAt(distance, "super");
    // "this" is always one level nearer than "super"'s environment.
    LoxInstance object = (LoxInstance)environment.getAt(distance - 1, "this");
    LoxFunction method = superclass.findMethod(object, expr.method.lexeme);

    if (method == null) {
      throw new RuntimeError(expr.method,
          "Undefined property '" + expr.method.lexeme + "'.");
    }

    if (((LoxFunction) method).isGetter()) {
      return ((LoxFunction) method).call(this, null);
    }

    return method;
  }

  @Override
  public Object visitThisExpr(Expr.This expr) {
    return lookUpVariable(expr.keyword, expr);
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitArrayExpr(Expr.Array expr) {
    return new LoxArray(expr.elements.stream()
        .map(this::evaluate)
        .collect(Collectors.toList()));
  }

  @Override
  public Object visitLogicalExpr(Expr.Logical expr) {
    Object left = evaluate(expr.left);

    if (expr.operator.type == OR) {
      if (isTruthy(left)) return left;
    } else {
      if (!isTruthy(left)) return left;
    }

    return evaluate(expr.right);
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case BANG:
        return !isTruthy(right);
      case MINUS:
        return -(double)right;
      case PLUS_PLUS: case MINUS_MINUS:
        if (!(expr.right instanceof Expr.Variable)) {
          throw new RuntimeError(expr.operator,
              "Operand of increment op must be a variable.");
        }
        checkNumberOperand(expr.operator, right);
        double value = (double) right;
        Expr.Variable var = (Expr.Variable) expr.right;
        double nextValue = expr.operator.type == PLUS_PLUS ? value + 1 : value - 1;
        environment.assign(var.name, nextValue);
        if (expr.postfix) {
          return value;
        } else {
          return nextValue;
        }
    }

    return null;
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number.");
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double) return;
    throw new RuntimeError(operator, "Operands must be numbers.");
  }

  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (Boolean)object;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

  public String stringify(Object object) {
    if (object == null) return "nil";

    // Hack. Work around Java adding ".0" to integer-valued doubles.
    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    if (object instanceof LoxArray) {
      List<String> elStrings = ((LoxArray) object).getElements().stream()
          .map(this::stringify)
          .collect(Collectors.toList());
      return "[" + String.join(", ", elStrings) + "]";
    }

    return object.toString();
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    LoxFunction function = new LoxFunction(stmt, environment, false);
    environment.define(stmt.name.lexeme, function);
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;
    if (stmt.value != null) {
      value = evaluate(stmt.value);
    }

    throw new ReturnJump(value);
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }

    environment.define(stmt.name.lexeme, value);
    return null;
  }

  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value);

    Integer distance = locals.get(expr);
    if (distance != null) {
      environment.assignAt(distance, expr.name, value);
    } else {
      globals.assign(expr.name, value);
    }

    environment.assign(expr.name, value);
    return value;
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return lookUpVariable(expr.name, expr);
  }

  private Object lookUpVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);
    if (distance != null) {
      return environment.getAt(distance, name.lexeme);
    } else {
      return globals.get(name);
    }
  }
}
