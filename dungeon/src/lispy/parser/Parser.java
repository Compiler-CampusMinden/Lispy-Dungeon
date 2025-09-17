package lispy.parser;

import static lispy.lexer.Token.TokenType.*;
import static lispy.utils.Error.error;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lispy.lexer.Lexer;
import lispy.lexer.Token;

/** LL(1) parser (recursive descent). */
public class Parser {
  private Lexer lexer;
  private Token lookahead;

  private Parser(Lexer lexer) {
    this.lexer = Objects.requireNonNull(lexer);
    this.lookahead = lexer.nextToken();
  }

  /**
   * Create a Parser from String.
   *
   * @param source source (string)
   * @return parsed AST
   */
  public static List<Expr> parseString(String source) {
    return new Parser(Lexer.from(source)).program();
  }

  /**
   * Create a Parser from File.
   *
   * @param path source (file)
   * @return parsed AST
   * @throws IOException when encountering issues while file handling
   */
  public static List<Expr> parseFile(Path path) throws IOException {
    return new Parser(Lexer.from(path)).program();
  }

  private List<Expr> program() {
    List<Expr> exprs = new ArrayList<>();
    while (lookahead.type() != EOF) {
      exprs.add(expression());
    }
    return exprs;
  }

  private Expr expression() {
    return switch (lookahead.type()) {
      case NUMBER -> number();
      case STRING -> string();
      case TRUE -> boolTrue();
      case FALSE -> boolFalse();
      case ID -> symbol();
      case LPAREN -> list();
      default -> throw error("unexpected token in expr: " + lookahead);
    };
  }

  private Expr number() {
    Token t = match(NUMBER);
    return new Expr.NumberLiteral(Integer.parseInt(t.lexeme()));
  }

  private Expr string() {
    Token t = match(STRING);
    return new Expr.StringLiteral(t.lexeme());
  }

  private Expr boolTrue() {
    match(TRUE);
    return new Expr.BoolLiteral(true);
  }

  private Expr boolFalse() {
    match(FALSE);
    return new Expr.BoolLiteral(false);
  }

  private Expr symbol() {
    Token t = match(ID);
    return new Expr.SymbolExpr(t.lexeme());
  }

  private Expr list() {
    match(LPAREN);

    List<Expr> elements = new ArrayList<>();

    switch (lookahead.type()) {
      case ID, OP -> {
        elements.add(new Expr.SymbolExpr(lookahead.lexeme()));
        consume();
      }
      default -> throw error("ID or OP expected, got " + lookahead);
    }

    while (isExprStart(lookahead.type())) {
      elements.add(expression());
    }

    match(RPAREN);
    return new Expr.ListExpr(elements);
  }

  private static boolean isExprStart(Token.TokenType t) {
    return switch (t) {
      case NUMBER, STRING, TRUE, FALSE, ID, LPAREN -> true;
      default -> false;
    };
  }

  private void consume() {
    lookahead = lexer.nextToken();
  }

  private Token match(Token.TokenType expected) {
    if (lookahead.type() != expected)
      throw error("expected: " + expected + ", found: " + lookahead);

    Token t = lookahead;
    consume();
    return t;
  }
}
