package md2html;

import java.io.*;
import java.lang.*;
import java.lang.Exception;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class Source {
    final int BufSize = 1024;
    final String CODE = "UTF8";

    protected int bufPos;
    protected int bufEnd;
    InputStreamReader input;
    private char[] buf;

    public char getChar() throws Exception {
        if (!hasNextChar()) {
            throw error("End of source");
        }
        return buf[bufPos];
    }

    public String readLine() throws Exception {
        StringBuilder ret = new StringBuilder();
        while (hasNextChar() && getChar() != '\n') {
            ret.append(readChar());
        }
        bufPos++;
        check();
        return ret.toString();
    }

    public boolean hasNextChar() throws Exception {
        return bufEnd != -1;
    }

    public char readChar() throws Exception {
        check();
        char ret = getChar();
        bufPos++;
        check();
        return ret;
    }

    public Source(InputStream inputStr) throws Exception {
        input = new InputStreamReader(inputStr);
        if (!Objects.equals(input.getEncoding(), CODE)) {
            throw error("Wrong encourfing");
        }
        buf = new char[BufSize];
        getChars();
    }

    private void check() throws Exception {
        if (bufPos == bufEnd) {
            getChars();
        }
    }

    private void getChars() throws Exception {
        bufPos = 0;
        do {
            bufEnd = input.read(buf, 0, BufSize);
        } while (bufEnd == 0);
    }
    public Exception error(String msg) throws Exception {
        return new Exception(msg);
    }
}
