package io.kurumi.ntt.utils;

import cn.hutool.core.util.StrUtil;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import cn.hutool.core.util.ArrayUtil;

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
            .escapeHtml(true)
           /* .attributeProviderFactory(new AttributeProviderFactory() {

                @Override
                public AttributeProvider create(AttributeProviderContext context) {

                    return new MDUIAttributeProvider();

                }

            }) */
            .build();

    }

    public static String parsePage(String title, String... content) {

        StringBuilder builder = new StringBuilder();

        builder.append("<html>");
        builder.append("<head>");

        builder.append("<meta charset=\"UTF-8\">");
        builder.append("<meta content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0;\" name=\"viewport\" />");
        //    builder.append("<link rel=\"stylesheet\" href=\"//cdnjs.loli.net/ajax/libs/mdui/0.4.2/css/mdui.min.css\">");
        //     builder.append("<script src=\"//cdnjs.loli.net/ajax/libs/mdui/0.4.2/js/mdui.min.js\"></script>");

        builder.append("<title>").append(title).append("</title>");

        builder.append("<style> img { height : auto; width:80%; } </style>");

        builder.append("</head>");

        // builder.append("<body class \"mdui-theme-primary-pink mdui-theme-accent-indigo\">")

        builder.append("<body style=\"overflow:auto;overflow-x: hidden\">").append(toHtml(content)).append("</body>");

        builder.append("</html>");

        return builder.toString();

    }
    
    public static String encodeAll(String source) {

        if (source == null) return null;
        
        return source

            .replace("\\", "\\\\")
            .replace("`", "\\`")
            .replace("*", "\\*")
            .replace("_", "\\_")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("#", "\\#")
            .replace("+", "\\+")
            .replace("-", "\\-")
            .replace(".", "\\.")
            .replace("!", "\\.");

    }

    public static String toHtml(String... content) {

        return renderer.render(parser.parse(ArrayUtil.join(content,"\n")));

    }

    public static String[] format(String[] content, Object... params) {

        for (int index = 0;index < content.length;index ++) {

            content[index] = format(content[index], params);

        }

        return content;

    }

    public static String format(String content, Object... params) {

        return StrUtil.format(content, params);

    }

    public static class MDUIAttributeProvider implements AttributeProvider {

        @Override
        public void setAttributes(Node node, String tagName, Map<String, String> attributes) {

            if (node instanceof Link) {

                attributes.put("class", "mdui-btn mdui-btn-raised mdui-ripple mdui-color-theme-accent");

            } else if (node instanceof TableBlock)  {

                attributes.put("class", "mdui-table mdui-table-hoverable");

            }

        }

    }

}
