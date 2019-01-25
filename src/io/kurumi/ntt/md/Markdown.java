package io.kurumi.ntt.md;

import java.util.*;
import org.commonmark.*;
import org.commonmark.parser.*;
import org.commonmark.renderer.html.*;
import org.commonmark.ext.autolink.*;
import org.commonmark.ext.gfm.strikethrough.*;
import org.commonmark.ext.gfm.tables.*;
import org.commonmark.ext.heading.anchor.*;
import org.commonmark.ext.ins.*;

public class Markdown {

    private static HtmlRenderer renderer;
    private static Parser parser;

    static {

        List<Extension> extensions = new LinkedList<>();

        extensions.add(AutolinkExtension.create());
        extensions.add(StrikethroughExtension.create());
        extensions.add(TablesExtension.create());
        extensions.add(HeadingAnchorExtension.builder().build());
        extensions.add(InsExtension.create());

        parser = Parser.builder()
            .extensions(extensions)
            .build();
            
        renderer = HtmlRenderer.builder()
            .extensions(extensions)
            .build();

    }

    public static String parsePage(String title, String content) {

        StringBuilder builder = new StringBuilder();

        builder.append("<html>");
        builder.append("<head>");

        builder.append("<meta content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0;\" name=\"viewport\" />");
        builder.append("<title>").append(title).append("</title>");
                
        builder.append("</head>");

        builder.append("<body>").append(toHtml(content)).append("</body>");

        builder.append("</html>");

        return builder.toString();

    }

    public static String toHtml(String content) {

        return renderer.render(parser.parse(content));

    }

    public static String[] encode(String[] content) {

        for (int index = 0;index < content.length;index ++) {

            content[index] = encode(content[index]);

        }

        return content;

    }

    public static String encode(String content) {

        return content.replace("_", "\\_");

    }

}
