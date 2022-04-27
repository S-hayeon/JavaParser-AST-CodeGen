package com.yourorganization.maven_sample.printer;
import com.github.javaparser.ast.Node;

public interface NodePrinter {


    /**
     * @param node The node to be printed - typically a CompilationUnit.
     * @return The formatted equivalent of node.
     */
    String output(Node node);

}