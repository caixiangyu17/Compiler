/**
 * *	Scanner.java
 **/

package VC.Scanner;

import VC.ErrorReporter;

public final class Scanner {

    private SourceFile sourceFile;
    private boolean debug;

    private ErrorReporter errorReporter;
    private StringBuffer currentSpelling;
    private char currentChar;
    private SourcePosition sourcePos;

    private int currentCharPos = 1;
    private int currentLinePos = 1;
    private char lastChar;
    private boolean isRollback = false;

    // =========================================================

    public Scanner(SourceFile source, ErrorReporter reporter) {
        sourceFile = source;
        errorReporter = reporter;
        currentChar = sourceFile.getNextChar();
        debug = false;

        // you may initialise your counters for line and column numbers here
    }

    public void enableDebugging() {
        debug = true;
    }

    // accept gets the next character from the source program.

    private void accept() {
        currentCharPos++;
        if (currentChar == '\n') {
            currentLinePos++;
            currentCharPos = 1;
        }
        lastChar = currentChar;
        currentChar = sourceFile.getNextChar();

        // you may save the lexeme of the current token incrementally here
        // you may also increment your line and column counters here
    }

    // inspectChar returns the n-th character after currentChar
    // in the input stream.
    //
    // If there are fewer than nthChar characters between currentChar
    // and the end of file marker, SourceFile.eof is returned.
    //
    // Both currentChar and the current position in the input stream
    // are *not* changed. Therefore, a subsequent call to accept()
    // will always return the next char after currentChar.

    private char inspectChar(int nthChar) {

        return sourceFile.inspectChar(nthChar);
    }

    private int nextToken() {
        // Tokens: separators, operators, literals, identifiers and keyworods

        if (isRollback) {
            switch (lastChar) {
                case '/':
                    currentSpelling.append('/');
                    sourcePos.charStart = --sourcePos.charFinish;
                    isRollback = false;
                    return Token.DIV;
            }
        }

        while (true) {
            switch (currentChar) {
                // separators
                case '|':
                    if (currentSpelling.length() == 0) {
                        accept();
                        if (currentChar == '|') {
                            accept();
                            currentSpelling.append("||");
                            sourcePos.charFinish = currentCharPos - 1;
                            return Token.OROR;
                        } else {
                            currentSpelling.append('|');
                            return Token.ERROR;
                        }
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }

                case '&':
                    if (currentSpelling.length() == 0) {
                        accept();
                        if (currentChar == '&') {
                            accept();
                            currentSpelling.append("&&");
                            sourcePos.charFinish = currentCharPos - 1;
                            return Token.ANDAND;
                        } else {
                            currentSpelling.append('&');
                            return Token.ERROR;
                        }
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }

                case '"':
                    accept();
                    boolean isEscapeSeq = false;
                    while (true) {
                        if (isEscapeSeq) {
                            switch (currentChar) {
                                case 'b':
                                    currentSpelling.deleteCharAt(currentSpelling
                                            .length() - 1);
                                    currentSpelling.append('\b');
                                    accept();
                                    break;
                                case 'f':
                                    currentSpelling.deleteCharAt(currentSpelling
                                            .length() - 1);
                                    currentSpelling.append('\f');
                                    accept();
                                    break;
                                case 'n':
                                    currentSpelling.deleteCharAt(currentSpelling
                                            .length() - 1);
                                    currentSpelling.append('\n');
                                    accept();
                                    break;
                                case 'r':
                                    currentSpelling.deleteCharAt(currentSpelling
                                            .length() - 1);
                                    currentSpelling.append('\r');
                                    accept();
                                    break;
                                case 't':
                                    currentSpelling.deleteCharAt(currentSpelling
                                            .length() - 1);
                                    currentSpelling.append('\t');
                                    accept();
                                    break;
                                case '\\':
                                    currentSpelling.deleteCharAt(currentSpelling
                                            .length() - 1);
                                    currentSpelling.append('\\');
                                    accept();
                                    break;
                                case '\'':
                                    currentSpelling.deleteCharAt(currentSpelling
                                            .length() - 1);
                                    currentSpelling.append('\'');
                                    accept();
                                    break;
                                case '\"':
                                    currentSpelling.deleteCharAt(currentSpelling
                                            .length() - 1);
                                    currentSpelling.append('\"');
                                    accept();
                                    break;
                                default:
                                    sourcePos.charFinish = currentCharPos - 1;
                                    errorReporter.reportError(
                                            "%: illegal escape character", "\\"
                                                    + currentChar, sourcePos);
                                    currentSpelling.append(currentChar);
                                    accept();
                                    break;

                            }
                        }

                        if (currentChar == '"') {
                            sourcePos.charFinish = currentCharPos;
                            accept();
                            return Token.STRINGLITERAL;
                        } else if (currentChar == '\\') {
                            isEscapeSeq = true;
                            currentSpelling.append(currentChar);
                            accept();
                        } else if (currentChar == '\n') {
                            errorReporter.reportError("%: unterminated string", currentSpelling.toString(),
                                    sourcePos);
                            sourcePos.charFinish = currentCharPos - 1;
                            return Token.STRINGLITERAL;
                        } else {
                            currentSpelling.append(currentChar);
                            isEscapeSeq = false;
                            accept();
                        }
                    }
                case ' ':
                    sourcePos.charFinish = currentCharPos - 1;
                    accept();
                    return getTokeType(currentSpelling.toString());
                case '\t':
                    sourcePos.charFinish = currentCharPos - 1;
                    currentCharPos = (currentCharPos / 8) * 8 + 8;
                    accept();
                    return getTokeType(currentSpelling.toString());
                case '(':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.LPAREN;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case ')':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.RPAREN;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case '{':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.LCURLY;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case '}':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.RCURLY;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case '[':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.LBRACKET;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case ']':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.RBRACKET;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case ';':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.SEMICOLON;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case '\n':
                    sourcePos.charFinish = currentCharPos - 1;
                    return getTokeType(currentSpelling.toString());
                // case '+':
                // if (currentSpelling.length() == 0) {
                // currentSpelling.append(currentChar);
                // accept();
                // return Token.PLUS;
                // } else {
                // sourcePos.charFinish = currentCharPos - 1;
                // return getTokeType(currentSpelling.toString());
                // }
                // case '-':
                // if (currentSpelling.length() == 0) {
                // currentSpelling.append(currentChar);
                // accept();
                // return Token.MINUS;
                // } else {
                // sourcePos.charFinish = currentCharPos - 1;
                // return getTokeType(currentSpelling.toString());
                // }
                case '*':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.MULT;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case '/':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.DIV;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case '+':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.PLUS;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case '-':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.MINUS;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case ',':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.COMMA;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case '<':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        if (currentChar == '=') {
                            sourcePos.charFinish = currentCharPos;
                            currentSpelling.append(currentChar);
                            accept();
                            return Token.LTEQ;
                        } else {
                            return Token.LT;
                        }

                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case '>':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        if (currentChar == '=') {
                            sourcePos.charFinish = currentCharPos;
                            currentSpelling.append(currentChar);
                            accept();
                            return Token.GTEQ;
                        } else {
                            return Token.GT;
                        }

                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case '=':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        if (currentChar == '=') {
                            sourcePos.charFinish = currentCharPos;
                            currentSpelling.append(currentChar);
                            accept();
                            return Token.EQEQ;
                        } else {
                            return Token.EQ;
                        }

                    } else if (currentSpelling.charAt(currentSpelling.length() - 1) == '!') {
                        sourcePos.charFinish = currentCharPos;
                        currentSpelling.append(currentChar);
                        accept();
                        return Token.NOTEQ;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case '!':
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(currentChar);
                        accept();
                        if (currentChar == '=') {
                            sourcePos.charFinish = currentCharPos;
                            currentSpelling.append(currentChar);
                            accept();
                            return Token.NOTEQ;
                        } else {
                            return Token.NOT;
                        }
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }
                case SourceFile.eof:
                    if (currentSpelling.length() == 0) {
                        currentSpelling.append(Token.spell(Token.EOF));
                        return Token.EOF;
                    } else {
                        sourcePos.charFinish = currentCharPos - 1;
                        return getTokeType(currentSpelling.toString());
                    }

                default:
                    currentSpelling.append(currentChar);
                    accept();
            }
        }
    }

    String isSigleToken(String token) {
        int floatCheckResult = floatCheck(token);
        if (floatCheckResult == 0) {
            sourcePos.charFinish = sourcePos.charStart + token.length() - 1;
            // Token tok = new Token(Token.FLOATLITERAL, token, sourcePos);
            // System.out.println(tok);
            return token;
        } else if (floatCheckResult == -1) {
            // System.out.println(token + " have nothing with float");
            boolean isStartWithDigit = false;
            if (token.charAt(0) >= '0' && token.charAt(0) <= '9') {
                isStartWithDigit = true;
            }
            if (isStartWithDigit) {
                for (int i = 0; i < token.length(); i++) {
                    if (token.charAt(i) < '0' || token.charAt(i) > '9') {
                        sourcePos.charFinish = sourcePos.charStart + i - 1;
                        Token tok = new Token(Token.INTLITERAL, token.substring(0,
                                i), sourcePos);
                        System.out.println(tok);
                        sourcePos.charStart = ++sourcePos.charFinish;
                        return isSigleToken(token.substring(i));
                    }
                }
            } else {
                for (int i = 0; i < token.length(); i++) {
                    if (token.charAt(i) == '.') {
                        if (i > 0) {
                            sourcePos.charFinish = sourcePos.charStart + i - 1;
                            Token tok = new Token(getTokeType(token.substring(0, i)),
                                    token.substring(0, i), sourcePos);
                            System.out.println(tok);
                            sourcePos.charStart = ++sourcePos.charFinish;
                            return isSigleToken(token.substring(i));
                        }
                        if (i == token.length() - 1) {
                            sourcePos.charStart = sourcePos.charFinish;
                            return ".";
                        } else {
                            sourcePos.charFinish = sourcePos.charStart;
                            Token tok = new Token(Token.ERROR, ".", sourcePos);
                            System.out.println(tok);
                            sourcePos.charStart = ++sourcePos.charFinish;
                            return isSigleToken(token.substring(i + 1));
                        }
                    } else if (token.charAt(i) == '+') {
                        if (i > 0) {
                            sourcePos.charFinish = sourcePos.charStart + i - 1;
                            Token tok = new Token(getTokeType(token.substring(0, i)),
                                    token.substring(0, i), sourcePos);
                            System.out.println(tok);
                            sourcePos.charStart = ++sourcePos.charFinish;
                            return isSigleToken(token.substring(i));
                        }
                        if (i == token.length() - 1) {
                            sourcePos.charStart = sourcePos.charFinish;
                            return "+";
                        } else {
                            Token tok = new Token(Token.PLUS, "+", sourcePos);
                            System.out.println(tok);
                            sourcePos.charStart = ++sourcePos.charFinish;
                            return isSigleToken(token.substring(i + 1));
                        }
                    } else if (token.charAt(i) == '-') {
                        if (i > 0) {
                            sourcePos.charFinish = sourcePos.charStart + i - 1;
                            Token tok = new Token(getTokeType(token.substring(0, i)),
                                    token.substring(0, i), sourcePos);
                            System.out.println(tok);
                            sourcePos.charStart = ++sourcePos.charFinish;
                            return isSigleToken(token.substring(i));
                        }
                        if (i == token.length() - 1) {
                            sourcePos.charStart = sourcePos.charFinish;
                            return "-";
                        } else {
                            Token tok = new Token(Token.MINUS, "-", sourcePos);
                            System.out.println(tok);
                            sourcePos.charStart = ++sourcePos.charFinish;
                            return isSigleToken(token.substring(i + 1));
                        }
                    }
                }
            }
            sourcePos.charFinish = sourcePos.charStart + token.length() - 1;
            return token;
        } else {
            sourcePos.charFinish = sourcePos.charStart + floatCheckResult - 1;
            Token tok = new Token(Token.FLOATLITERAL, token.substring(0,
                    floatCheckResult), sourcePos);
            System.out.println(tok);
            sourcePos.charStart = ++sourcePos.charFinish;
            return isSigleToken(token.substring(floatCheckResult));
        }

    }

    int floatCheck(String token) {
        final int STATE_INIT = 0x0000;
        final int STATE_DIGIT = 0x0001;
        final int STATE_DOT = 0x0002;
        final int STATE_FLOAT = 0x0003;
        final int STATE_E = 0x0004;
        final int STATE_TIMES_SIGN = 0x0005;
        final int STATE_TIMES = 0x0006;
        final int STATE_ERROR = 0x0007;
        final int STATE_FLOAT_WITH_OTHER_TOKENS = 0x0008;

        int state = STATE_INIT;
        boolean isFloat = false;
        boolean beginWithFloat = false;
        boolean withDot = false;
        boolean withIntPart = false;
        boolean withFloatPart = false;
        int floatEndPosition = -1;

        StringBuffer digits_int_part = new StringBuffer();
        StringBuffer digits_float_part = new StringBuffer();
        char eChar;
        StringBuffer timesString = new StringBuffer();
        for (int i = 0; i < token.length(); i++) {
            char tempChar = token.charAt(i);
            if (state == STATE_E) {
                if (tempChar != '+' && tempChar != '-' && (tempChar <= '0' || tempChar >= '9')) {
                    if (withDot && (withIntPart || withFloatPart)) {
                        state = STATE_FLOAT_WITH_OTHER_TOKENS;
                        floatEndPosition = i - 1;
                    } else {
                        state = STATE_ERROR;
                    }
                    break;
                }
            } else if (state == STATE_TIMES_SIGN) {
                if (tempChar <= '0' || tempChar >= '9') {
                    state = STATE_FLOAT_WITH_OTHER_TOKENS;
                    floatEndPosition = i - 2;
                    break;
                }
            }
            if (tempChar >= '0' && tempChar <= '9') {
                switch (state) {
                    case STATE_INIT:
                        withIntPart = true;
                        state = STATE_DIGIT;
                        digits_int_part.append(tempChar);
                        break;
                    case STATE_DIGIT:
                        withIntPart = true;
                        state = STATE_DIGIT;
                        digits_int_part.append(tempChar);
                        break;
                    case STATE_DOT:
                        state = STATE_FLOAT;
                        digits_float_part.append(tempChar);
                        break;
                    case STATE_FLOAT:
                        withIntPart = true;
                        state = STATE_FLOAT;
                        digits_float_part.append(tempChar);
                        break;
                    case STATE_E:
                        state = STATE_TIMES;
                        timesString.append(tempChar);
                        break;
                    case STATE_TIMES_SIGN:
                        state = STATE_TIMES;
                        timesString.append(tempChar);
                        break;
                    case STATE_TIMES:
                        state = STATE_TIMES;
                        timesString.append(tempChar);
                        break;
                    default:
                        state = STATE_ERROR;
                        break;
                }
            } else if (tempChar == '.') {
                withDot = true;
                if (state == STATE_INIT || state == STATE_DIGIT) {
                    state = STATE_DOT;
                } else if (state == STATE_TIMES || state == STATE_FLOAT) {
                    state = STATE_FLOAT_WITH_OTHER_TOKENS;
                    floatEndPosition = i;
                    break;
                } else {
                    state = STATE_ERROR;
                    break;
                }
            } else if (tempChar == 'e' || tempChar == 'E') {
                if (state == STATE_DIGIT || state == STATE_DOT
                        || state == STATE_FLOAT) {
                    state = STATE_E;
                    eChar = tempChar;
                } else if (state == STATE_TIMES) {
                    state = STATE_FLOAT_WITH_OTHER_TOKENS;
                    floatEndPosition = i;
                    break;
                } else {
                    state = STATE_ERROR;
                    break;
                }
            } else if (tempChar == '+' || tempChar == '-') {
                if (state == STATE_E) {
                    state = STATE_TIMES_SIGN;
                    timesString.append(tempChar);
                } else if (state == STATE_TIMES) {
                    state = STATE_FLOAT_WITH_OTHER_TOKENS;
                    floatEndPosition = i;
                    break;
                } else {
                    state = STATE_ERROR;
                    break;
                }
            } else {
                if (state == STATE_TIMES || state == STATE_FLOAT) {
                    state = STATE_FLOAT_WITH_OTHER_TOKENS;
                    floatEndPosition = i;
                    break;
                } else if (state == STATE_DOT && i != 1) {
                    state = STATE_FLOAT_WITH_OTHER_TOKENS;
                    floatEndPosition = i;
                    break;
                } else {
                    state = STATE_ERROR;
                    break;
                }
            }

        }
        if (state == STATE_TIMES) {
//			System.out.println(token + " is a float with all parts");
            isFloat = true;
        } else if (state == STATE_DOT) {
            if (!digits_int_part.toString().isEmpty() || !digits_float_part.toString().isEmpty()) {
//				System.out.println(token + " is a float only with dot");
                isFloat = true;
            } else {
//				System.out.println(token + " is just a dot");
                isFloat = false;
            }
        } else if (state == STATE_FLOAT) {
//			System.out.println(token + " is a float without times");
            isFloat = true;
        } else if (state == STATE_TIMES_SIGN) {
            beginWithFloat = true;
            floatEndPosition = token.length() - 2;
        } else if (state == STATE_E) {
            beginWithFloat = true;
            floatEndPosition = token.length() - 1;
        } else if (state == STATE_FLOAT_WITH_OTHER_TOKENS) {
            // System.out
            // .println(token + " is not a float but begin with a float");
            beginWithFloat = true;
        }
        //System.out.println(state);
        if (isFloat) {
            return 0;
        } else if (beginWithFloat) {
            return floatEndPosition;
        } else {
            return -1;
        }
    }

    int getTokeType(String token) {

        token = isSigleToken(token);
        currentSpelling = new StringBuffer(token);

        if (floatCheck(token) == 0) {
            return Token.FLOATLITERAL;
        }

        boolean isInt = true;
        for (int i = 0; i < token.length(); i++) {
            if (token.charAt(i) < '0' || token.charAt(i) > '9') {
                isInt = false;
                break;
            }
        }
        if (isInt) {
            return Token.INTLITERAL;
        }
        switch (token) {
            // reserved words
            case "boolean":
                return Token.BOOLEAN;
            case "break":
                return Token.BREAK;
            case "continue":
                return Token.CONTINUE;
            case "else":
                return Token.ELSE;
            case "float":
                return Token.FLOAT;
            case "for":
                return Token.FOR;
            case "if":
                return Token.IF;
            case "int":
                return Token.INT;
            case "return":
                return Token.RETURN;
            case "void":
                return Token.VOID;
            case "while":
                return Token.WHILE;
            // operators
            case "+":
                return Token.PLUS;
            case "-":
                return Token.MINUS;
            case "*":
                return Token.MULT;
            case "/":
                return Token.DIV;
            case "!=":
                return Token.NOTEQ;
            case "!":
                return Token.NOT;
            case "=":
                return Token.EQ;
            case "==":
                return Token.EQEQ;
            case "<":
                return Token.LT;
            case "<=":
                return Token.LTEQ;
            case ">":
                return Token.GT;
            case ">=":
                return Token.GTEQ;
            case "&&":
                return Token.ANDAND;
            case "||":
                return Token.OROR;
            case "true":
                return Token.BOOLEANLITERAL;
            case "false":
                return Token.BOOLEANLITERAL;
            case ".":
                return Token.ERROR;
            default:
                return Token.ID;
        }
    }

    void skipSpaceAndComments() {
        switch (currentChar) {
            case '\t':
                accept();
                currentCharPos = (currentCharPos / 8) * 8 + 8 + 1;
                skipSpaceAndComments();
                break;
            case '\n':
                accept();
                skipSpaceAndComments();
                break;
            case ' ':
                accept();
                skipSpaceAndComments();
                break;
            case '/':
                accept();
                if (currentChar == '/') {
                    accept();
                    while (currentChar != '\n') {
                        accept();
                    }
                    skipSpaceAndComments();
                } else if (currentChar == '*') {
                    SourcePosition position = new SourcePosition();
                    position.lineStart = position.lineFinish = currentLinePos;
                    position.charStart = position.charFinish = currentCharPos - 1;
                    boolean isStar = false;
                    while (true) {
                        accept();
                        if (isStar && currentChar == '/') {
                            accept();
                            break;
                        }
                        if (currentChar == '*') {
                            isStar = true;
                        } else {
                            isStar = false;
                        }
                        if (currentChar == SourceFile.eof) {

                            errorReporter.reportError(": unterminated comment", null,
                                    position);
                            break;
                        }
                    }
                    skipSpaceAndComments();
                } else {
                    isRollback = true;
                }
        }

    }

    public Token getToken() {
        Token tok;
        int kind;
        // skip white space and comments

        skipSpaceAndComments();

        currentSpelling = new StringBuffer("");

        sourcePos = new SourcePosition();
        sourcePos.lineStart = sourcePos.lineFinish = currentLinePos;
        sourcePos.charStart = sourcePos.charFinish = currentCharPos;
        // You must record the position of the current token somehow

        kind = nextToken();

        tok = new Token(kind, currentSpelling.toString(), sourcePos);

        // * do not remove these three lines
        if (debug)
            System.out.println(tok);
        return tok;
    }

}
