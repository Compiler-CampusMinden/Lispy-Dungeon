package lispy.lexer;

/**
 * Token.
 *
 * @param type type
 * @param lexeme lexeme
 */
public record Token(TokenType type, String lexeme) {
  /**
   * create a new token.
   *
   * @param type type
   * @param lexeme lexeme
   * @return new token
   */
  public static Token of(TokenType type, String lexeme) {
    return new Token(type, lexeme);
  }

  /** Token type. */
  public enum TokenType {
    /** '('. */
    LPAREN,
    /** ')'. */
    RPAREN,
    /** 'true'. */
    TRUE,
    /** 'false'. */
    FALSE,
    /** '[a-z][a-zA-Z0-9]*'. */
    ID,
    /** '[0-9]+'. */
    NUMBER,
    /** "'+' | '-' | '*' | '/' | '=' | '>' | '<'". */
    OP,
    /** '"' (~[\n\r"])* '"'. */
    STRING,
    /** eof. */
    EOF
  }
}
