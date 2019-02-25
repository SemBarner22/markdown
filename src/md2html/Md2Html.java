package md2html;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.*;


public class Md2Html {
    public static void main(String[] args) throws java.lang.Exception {
        if (args.length != 2) {
            System.out.println();
        }
        Source src = new Source(new FileInputStream(args[0]));
        Out out = new Out(new FileOutputStream(args[1]));

        ParserParagraph next = new ParserParagraph(src, out);
        next.convert();
        out.close();
    }
}