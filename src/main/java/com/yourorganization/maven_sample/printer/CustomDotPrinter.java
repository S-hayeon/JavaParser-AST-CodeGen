package com.yourorganization.maven_sample.printer;


import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import org.apache.commons.text.StringEscapeUtils;

import java.util.List;

import static com.github.javaparser.utils.Utils.assertNotNull;
import static java.util.stream.Collectors.toList;

/**
 * Outputs a Graphviz diagram of the AST.
 */
public class CustomDotPrinter implements NodePrinter {

    private static final int DEFAULT_STRINGBUILDER_CAPACITY = 5000;

    private static final boolean DEFAULT_RESOLVE_TYPES = false;

    private final boolean outputNodeType;
    private       int     nodeCount;


    public CustomDotPrinter(final boolean outputNodeType) {
        this.outputNodeType = outputNodeType;
        this.nodeCount = 0;
    }


    private static String escape(String value) {
        return value.replace("\"", "\\\"");
    }


    private String nextNodeName() {
        return "n" + (this.nodeCount++);
    }


    /**
     * @param node         The node to be printed - typically a CompilationUnit.
     * @param resolveTypes Should node types be resolved?
     * @return The DOT-formatted equivalent of node.
     */
    public String output(final Node node, final boolean resolveTypes) {
        this.nodeCount = 0;
        final StringBuilder output = new StringBuilder(DEFAULT_STRINGBUILDER_CAPACITY);
        output.append("digraph {");
        this.output(node, null, "root", output, resolveTypes);
        output.append(System.lineSeparator()).append("}");
        return output.toString();
    }


    public void output(final Node node, final String parentNodeName, final String name, final StringBuilder builder) {
        this.output(node, parentNodeName, name, builder, DEFAULT_RESOLVE_TYPES);
    }


    public void output(final Node node, final String parentNodeName, final String name, final StringBuilder builder, final boolean resolveTypes) {
        assertNotNull(node);
        final NodeMetaModel           metaModel             = node.getMetaModel();
        final List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();
        final List<PropertyMetaModel> attributes            = allPropertyMetaModels.stream().filter(PropertyMetaModel::isAttribute).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subNodes              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNode).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subLists              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNodeList).collect(toList());

        final String typeName = metaModel.getTypeName();
        String       range    = "";

        // Custom: If range is present, add it.
        if (node.getRange().isPresent()) {
            range += "";
            range += this.rangeAsString(node.getRange().get());
            range += "";
        }


        final String lineColor;
        final String lineLabel;
        if ("comment".equals(name)) {
//            lineColor = "gray";
            lineColor = "LightGray";
//            lineLabel = "comment";
            lineLabel = "";
        } else if ("name".equals(name)) {
//            lineColor="darkgreen";
//            lineColor="blue";
//            lineColor="SlateBlue";
            lineColor = "SteelBlue";
//            lineLabel = "name";
            lineLabel = "";
        } else if ("StringLiteralExpr".equals(typeName)) {
//        } else if (typeName.endsWith("LiteralExpr")) {
//            lineColor="SlateBlue";
            lineColor = "SeaGreen";
            lineLabel = "Literal Expression";
//            lineLabel = "LiteralExpr";
        } else {
            lineColor = "black";
            lineLabel = "";
        }

        final String  ndName  = this.nextNodeName();
        StringBuilder nodeDot = new StringBuilder(DEFAULT_STRINGBUILDER_CAPACITY);
        nodeDot.append(System.lineSeparator());
        nodeDot.append(ndName);
        nodeDot.append(" [");
        nodeDot.append("shape=none");
        nodeDot.append(",");
        nodeDot.append("label=<");

        nodeDot.append("<font color='").append(lineColor).append("'>");

        nodeDot.append("<table" + " border='0'" + " color='").append(lineColor).append("'").append(" cellspacing='0'").append(" cellborder='1'").append(">");
        nodeDot.append("<tr>");
        nodeDot.append("<td colspan='2'>");
        nodeDot.append("<font color='").append(lineColor).append("'>");
        nodeDot.append(escape(name));
        if (this.outputNodeType) {
            nodeDot.append(" (").append(typeName).append(")");
        }
        nodeDot.append("</font>");
        nodeDot.append("<br/>");
        nodeDot.append("<font color='#aaaaaa' size='8'>");
        nodeDot.append(range);
        nodeDot.append("</font>");


        if (resolveTypes && node instanceof Expression) {
            final Expression bar = (Expression) node;

            String returnTypeString = null;

            try {
//                if (!bar.toString().equals("System") && !bar.toString().equals("String")) {
                ResolvedType returnType = bar.calculateResolvedType();
                returnTypeString = StringEscapeUtils.escapeHtml4(returnType.describe());
//                }
            } catch (final UnsolvedSymbolException e) {
//                returnTypeString = "Unable to resolve type of " + bar + " (UnsolvedSymbolException)";
                System.err.println("Unable to resolve type of " + bar + " (UnsolvedSymbolException)");
                e.printStackTrace();
            } catch (final Exception e) {
//                returnTypeString = "Unable to resolve type of " + bar + " (Exception - " + e.getClass().getName() + ")";
                System.err.println("Unable to resolve type of " + bar + " (Exception - " + e.getClass().getName() + ")");
                e.printStackTrace();
            }

            if (returnTypeString != null) {
                nodeDot.append("<br/>");
                nodeDot.append("<font color='red' size='8'>");
                nodeDot.append("Resolved Type: ");
                nodeDot.append(returnTypeString);
                nodeDot.append("</font>");
            }
        }

        nodeDot.append("</td>");
        nodeDot.append("</tr>");

        for (final PropertyMetaModel a : attributes) {
            nodeDot.append("<tr>");
            nodeDot.append("<td>").append(a.getName()).append("</td>");
            nodeDot.append("<td align='left'>");

            String   value = a.getValue(node).toString();
            String[] lines = value.trim().split("\\r?\\n");

            String cellAlignment = lines.length > 1 ? "left" : "center";
            nodeDot.append("<table border='0' cellspacing='0' cellpadding='0'>");
            for (final String line : lines) {
                nodeDot.append("<tr><td align='").append(cellAlignment).append("'>").append(StringEscapeUtils.escapeHtml4(line)).append("</td></tr>");
            }
            nodeDot.append("</table>");

            nodeDot.append("</td>");
            nodeDot.append("</tr>");
        }

        nodeDot.append("</table>");

        nodeDot.append("</font>");
        nodeDot.append(">];");

        builder.append(nodeDot.toString());


        if (parentNodeName != null) {
            builder.append(System.lineSeparator())
                    .append(parentNodeName).append(" -> ").append(ndName)
                    .append(" [").append("color=").append(lineColor).append(", fontcolor=").append(lineColor).append(", label=\"").append(lineLabel).append("\"").append("]")
                    .append(";");
        }

        for (final PropertyMetaModel sn : subNodes) {
            final Node nd = (Node) sn.getValue(node);
            if (nd != null) {
                this.output(nd, ndName, sn.getName(), builder, resolveTypes);
            }
        }

        String color;
        String label;

        for (final PropertyMetaModel sl : subLists) {
            final NodeList<? extends Node> nl = (NodeList<? extends Node>) sl.getValue(node);
            if (nl != null && nl.isNonEmpty()) {
//                color = "FireBrick";
//                color = "red";
                color = "OrangeRed";
                label = "property list";

                final String ndLstName = this.nextNodeName();
                builder.append(System.lineSeparator()).append(ndLstName).append(" [shape=ellipse,color=").append(color).append(",label=\"").append(escape(sl.getName())).append("\"];");
                builder.append(System.lineSeparator()).append(ndName).append(" -> ")
                        .append(ndLstName)
                        .append(" [").append("color=").append(color).append(", fontcolor=").append(color).append(", label=\"").append(label).append("\"").append("]");
//                       .append(" [color = ").append(color).append("];");
                final String slName = sl.getName().substring(0, sl.getName().length() - 1);
                for (final Node nd : nl) {
                    this.output(nd, ndLstName, slName, builder, resolveTypes);
                }
            }
        }
    }


    @Override
    public String output(final Node node) {
        return this.output(node, DEFAULT_RESOLVE_TYPES);
    }


    private String rangeAsString(final Range range) {
        final int startLine   = range.begin.line;
        final int startColumn = range.begin.column;
        final int endLine     = range.end.line;
        final int endColumn   = range.end.column;

        return "[" + startLine + ":" + startColumn + "-" + endLine + ":" + endColumn + "]";
    }


    @Override
    public String toString() {
        return "CustomDotPrinter{" +
                "outputNodeType=" + this.outputNodeType +
                ", nodeCount =" + this.nodeCount +
                '}';
    }
}