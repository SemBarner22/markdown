package md2html;

import java.io.IOException;
import java.lang.*;
import java.lang.Exception;
import java.util.ArrayList;


public class ParserParagraph {
    private Source source;
    private Out out;

    private void skip() throws java.lang.Exception {
        while (source.hasNextChar() && (source.getChar() == '\n' || source.getChar() == '\r')) {
            source.readLine();
        }
    }

    public void convert() throws Exception {
        while (source.hasNextChar()) {
            skip();
            if (!source.hasNextChar()) {
                break;
            }
            ArrayList<String> strings = new ArrayList<>();
            while (source.hasNextChar() &&  !isEmptyLine()) {
                strings.add(source.readLine());
            }
            i = 0;
            parseHead(strings);
        }
    }

    private int now = 0;


    private boolean boldStar;
    private boolean boldUnder;
    private boolean strongBold;
    private boolean code;
    private boolean mark;
    private boolean under;
    private boolean picture;
    private boolean sourc;
    private boolean strikethrough;
    private boolean opened;
    private StringBuilder http = new StringBuilder();

    private void parseHead(ArrayList<String> strings) throws Exception {
        if (strings.get(0).charAt(now) == '#') {
            while (strings.get(0).charAt(now) == '#') {
                now++;
            }
            if (strings.get(0).charAt(now) == ' ' && now < 7) {
                out.print("<h" + now + '>');
                int par = now;
                now++;
                parseMainBody(strings);
                out.println("</h" + par + '>');
            } else {
                now = 0;
                parseParagraph(strings);
            }
        } else {
            parseParagraph(strings);
        }
    }

    private void parseParagraph(ArrayList<String> strs) throws Exception {
        out.print("<p>");
        parseMainBody(strs);
        out.println("</p>");
    }

    private int i;

    private void parseMainBody(ArrayList<String> strs) throws Exception {
        StringBuilder str = new StringBuilder();
        while (i < strs.size()) {
            String s = strs.get(i);
            while (now < s.length() && s.charAt(now) != '\r' && s.charAt(now) != '\n') {
                if (picture && s.charAt(now) != ']') {
                    str.append(s.charAt(now));
                    now++;
                    continue;
                }
                if (s.charAt(now) == '*') {
                    if (!opened) {
                        str.append(testBold1(s));
                    } else {
                        http.append(testBold1(s));
                    }
                    continue;
                }
                if (!opened && s.charAt(now) == '!' && now < s.length() - 1 && s.charAt(now + 1) == '[') {
                    picture = true;
                    now++;
                    str.append(testOpenBrace(s));
                    continue;
                }
                if (s.charAt(now) == '_') {
                    if (!opened) {
                        str.append(testBold2(s));
                    } else {
                        http.append(testBold2(s));
                    }
                    continue;
                }
                if (s.charAt(now) == '-') {
                    if (!opened) {
                        str.append(testStrikeThrough(s));
                    } else {
                        http.append(testStrikeThrough(s));
                    }
                    continue;
                }
                if (s.charAt(now) == '+') {
                    if (!opened) {
                        str.append(testUnder(s));
                    } else {
                        http.append(testUnder(s));
                    }
                    continue;
                }
                if (s.charAt(now) == '~') {
                    if (!opened) {
                        str.append(testMark(s));
                    } else {
                        http.append(testMark(s));
                    }
                    continue;
                }
                if (s.charAt(now) == '`') {
                    if (!opened) {
                        str.append(testCode(s));
                    } else {
                        http.append(testCode(s));
                    }
                    continue;
                }
                if (s.charAt(now) == '(') {
                    if (now > 0 && s.charAt(now - 1) == ']') {
                        if (sourc) {
                            str.append(testPict(strs));
                            continue;
                        } else {
                            str.append(testHttp(strs));
                            continue;
                        }
                    } else {
                        str.append('(');
                        now++;
                        continue;
                    }
                }
                if (s.charAt(now) == '[') {
                    str.append(testOpenBrace(s));
                    opened = true;
                    continue;
                }
                if (s.charAt(now) == ']') {
                    if (picture) {
                        str.append(testPicture(s));
                        sourc = true;
                    }
                    opened = false;
                    picture = false;
                    now++;
                } else {
                    if (!opened) {
                        str.append(testChar(s));
                    } else {
                        http.append(testChar(s));
                    }
                }
            }
            if (i != strs.size() - 1) {
                if (!opened) {
                    str.append('\n');
                } else {
                    http.append('\n');
                }
            }
            now = 0;
            i++;
        }
        out.print(str.toString());
    }

    private String testPicture(String s) {
        return "' src='";
    }

    private String testMark(String s) {
        now++;
        mark = !mark;
        if (mark) {
            return "<mark>";
        } else {
            return "</mark>";
        }
    }

    private String testPict(ArrayList<String> strin) {
        StringBuilder str = new StringBuilder();
        now++;
        while (i < strin.size() && now < strin.get(i).length() && strin.get(i).charAt(now) != ')') {
            str.append(strin.get(i).charAt(now));
            now++;
            if (now == strin.get(i).length()) {
                i++;
                now = 0;
            }
        }
        now++;
        str.append("'>");
        return str.toString();
    }

    private String testHttp(ArrayList<String> strin) {
        StringBuilder str = new StringBuilder();
        now++;
        while (i < strin.size() && now < strin.get(i).length() && strin.get(i).charAt(now) != ')') {
            str.append(strin.get(i).charAt(now));
            now++;
            if (now == strin.get(i).length()) {
                i++;
                now = 0;
            }
        }
        now++;
        str.append("'>").append(http).append("</a>");
        http = new StringBuilder();
        return str.toString();
    }

    private String testOpenBrace(String s) {
        StringBuilder str = new StringBuilder();
        if (!picture) {
            str.append("<a href='");
        } else {
            str.append("<img alt='");
        }
        now++;
        return str.toString();
    }

    private String testChar(String s) {
        now++;
        switch (s.charAt(now - 1)) {
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '&':
                return "&amp;";
            case '\\':
                if (now < s.length() && (s.charAt(now) == '*' || s.charAt(now) == '_')) {
                    now++;
                    return String.valueOf(s.charAt(now - 1));
                }
            default:
                return String.valueOf(s.charAt(now - 1));
        }
    }

    private String testCode(String s) {
        now++;
        code = !code;
        if (code) {
            return "<code>";
        } else {
            return "</code>";
        }
    }

    private String testStrikeThrough(String s) throws Exception {
        int k = amountOfChars(s);
        switch (k) {
            case 1:
                now++;
                return String.valueOf('-');
            case 2:
                now += 2;
                strikethrough = !strikethrough;
                if (strikethrough) {
                    return "<s>";
                } else {
                    return "</s>";
                }
            default:
                throw new Exception("It's more than 2 dashes");
        }
    }

    private String testUnder(String s) throws Exception {
        int k = amountOfChars(s);
        switch (k) {
            case 1:
                now++;
                return String.valueOf('+');
            case 2:
                now += 2;
                under = !under;
                if (under) {
                    return "<u>";
                } else {
                    return "</u>";
                }
            default:
                throw new Exception("It's more than 2 dashes");
        }
    }

    private String testBold1(String s) throws Exception {
        int k = amountOfChars(s);
        switch (k) {
            case 1:
                now++;
                if ((now == s.length() || s.charAt(now) == ' ' || s.charAt(now) == '\n') && (now == 1 ||s.charAt(now - 2) == ' ')) {
                    return String.valueOf(s.charAt(now - 1));
                }
                boldStar = !boldStar;
                if (boldStar) {
                    return "<em>";
                } else {
                    return "</em>";
                }
            case 2:
                now += 2;
                strongBold = !strongBold;
                if (strongBold) {
                    return "<strong>";
                } else {
                    return "</strong>";
                }
            default:
                throw new Exception("It's more than 2 bolds");
        }
    }

    private String testBold2(String s) throws Exception {
        int k = amountOfChars(s);
        switch (k) {
            case 1:
                now++;
                if ((now == s.length() || s.charAt(now) == ' ' || s.charAt(now) == '\n') && (now == 1 ||s.charAt(now - 2) == ' ')) {
                    return String.valueOf(s.charAt(now - 1));
                }
                boldUnder = !boldUnder;
                if (boldUnder) {
                    return "<em>";
                } else {
                    return "</em>";
                }
            case 2:
                now += 2;
                strongBold = !strongBold;
                if (strongBold) {
                    return "<strong>";
                } else {
                    return "</strong>";
                }
            default:
                throw new Exception("It's more than 2 bolds");
        }
    }


    private int amountOfChars(String s) {
        int ans = 0;
        int pos = now;
        int ch = s.charAt(now);
        while (pos < s.length() && s.charAt(pos) == ch) {
            ans++;
            pos++;
        }
        return ans;
    }


    private boolean isEmptyLine() throws Exception {
        if (source.hasNextChar() && (source.getChar() != '\n' && source.getChar() != '\r')) {
            return false;
        }
        return true;
    }

    ParserParagraph(Source src, Out out) {
        this.source = src;
        this.out = out;
    }
}