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
}
