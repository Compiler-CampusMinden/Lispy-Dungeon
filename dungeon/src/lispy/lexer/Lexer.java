package lispy.lexer;

import static lispy.lexer.Token.TokenType.*;
import static lispy.utils.Error.*;

import java.util.Objects;
import java.util.function.Predicate;

/** Lexer (recursive descent). */
public class Lexer {
  private String input;
  private int index = 0;

  /**
   * Create a Lexer from String.
   *
   * @param input source (string)
   */
  public Lexer(String input) {
    this.input = Objects.requireNonNull(input);
  }

  /**
   * Read the next token.
   *
   * @return next token
   */
  public Token nextToken() {
    goobleWhiteSpaceAndComments();

    if (!eof()) {
      char c = consume();
      return switch (c) {
        case '(' -> new Token(LPAREN, "(");
        case ')' -> new Token(RPAREN, ")");
        case '+', '-', '*', '/', '=', '>', '<' -> new Token(OP, String.valueOf(c));
        case '"' -> new Token(STRING, readStringLiteral());
        default -> {
          if (isDigit(c)) yield new Token(NUMBER, c + readWhile(Lexer::isDigit));
          if (isLetter(c)) {
            String id = c + readWhile(Lexer::isLetterOrDigit);
            yield switch (id) {
              case "true" -> new Token(TRUE, id);
              case "false" -> new Token(FALSE, id);
              default -> new Token(ID, id);
            };
          }
          throw error("unexpected character '" + c + "'");
        }
      };
    }
    return new Token(EOF, "<EOF>");
  }

  private boolean eof() {
    return index >= input.length();
  }

  private char peek() {
    return (index < input.length()) ? input.charAt(index) : '\0';
  }

  private char consume() {
    return (index < input.length()) ? input.charAt(index++) : '\0';
  }

  private boolean match(char c) {
    if (peek() != c) return false;
    consume();
    return true;
  }

  private String readWhile(Predicate<Character> pred) {
    StringBuilder sb = new StringBuilder();
    while (!eof() && pred.test(peek())) sb.append(consume());
    return sb.toString();
  }

  private String readStringLiteral() {
    String s = readWhile(c -> c != '"' && c != '\n' && c != '\r');

    if (peek() == '"') consume(); // closing '"'
    else if (peek() == '\n' || peek() == '\r')
      throw error("string not terminated (found line end before matching '\"')");
    else if (peek() == '\0') throw error("string not terminated (found EOF before matching '\"')");

    return s;
  }

  private void goobleWhiteSpaceAndComments() {
    while (!eof()) {
      switch (peek()) {
        case ';' -> skipComments();
        case ' ', ',', '\t', '\n', '\r' -> skipWhitespace();
        default -> {
          return;
        }
      }
    }
  }

  private void skipComments() {
    if (match(';')) readWhile(c -> c != '\n' && c != '\r');
  }

  private void skipWhitespace() {
    readWhile(c -> c == ' ' || c == ',' || c == '\t' || c == '\n' || c == '\r');
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isLetter(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }

  private static boolean isLetterOrDigit(char c) {
    return isLetter(c) || isDigit(c);
  }
}
