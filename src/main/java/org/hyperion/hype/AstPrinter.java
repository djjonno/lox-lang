package org.hyperion.hype;

// Creates an unambiguous, if ugly, string representation of AST nodes.
public class AstPrinter implements Expr.Visitor<String> {

  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null) return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  @Override
  public String visitVariableExpr(Expr.Variable expr) {
    return parenthesize("var", expr);
  }

  @Override
  public String visitAssignExpr(Expr.Assign expr) {
    return parenthesize("assign", expr);
  }

  @Override
  public String visitSetExpr(Expr.Set expr) {
    return expr.name.lexeme;
  }

  @Override
  public String visitLogicalExpr(Expr.Logical expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitGetExpr(Expr.Get expr) {
    return expr.name.toString();
  }

  @Override
  public String visitCallExpr(Expr.Call expr) {
    StringBuilder argList = new StringBuilder();

    for (Expr arg : expr.arguments) {
      argList.append(arg.accept(this)).append(", ");
    }

    return expr.callee.accept(this) + "(" + argList.toString() + ")";
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }
}
