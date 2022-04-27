package com.yourorganization.maven_sample;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class CodeGenerator {
    //https://dzone.com/articles/javaparser-java-code-generation

    public static void main(String[] args) {
        CompilationUnit compilationUnit = new CompilationUnit();
        // declare package
        compilationUnit.setPackageDeclaration("tmax");

        // import statement
        compilationUnit.addImport("java.util.ArrayList", false, false);

        // Create a class
        ClassOrInterfaceDeclaration myClass = compilationUnit.addClass("HelloWorld", Modifier.Keyword.PUBLIC);


        // set  Method
        MethodDeclaration main = myClass.addMethod("main", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);

        // set a parameter: the value to add to the field
        main.addParameter("String[]", "args");

        main.getBody().get().getStatements().add(new ExpressionStmt(
                new MethodCallExpr(new NameExpr("System"), new SimpleName("out.println"),
                        NodeList.nodeList(new NameExpr("hello world")))
        ));

        BlockStmt blockStmt = new BlockStmt();
        main.setBody(blockStmt);
        ExpressionStmt expressionStmt = new ExpressionStmt();
        VariableDeclarationExpr variableDeclarationExpr = new VariableDeclarationExpr();
        VariableDeclarator variableDeclarator = new VariableDeclarator();
        variableDeclarator.setName("arr");
        variableDeclarator.setType("ArrayList<String>");
        variableDeclarator.setInitializer("new ArrayList<>()");
        NodeList<VariableDeclarator> variableDeclarators = new NodeList<>();
        variableDeclarators.add(variableDeclarator);
        variableDeclarationExpr.setVariables(variableDeclarators);
        expressionStmt.setExpression(variableDeclarationExpr);
        blockStmt.addStatement(expressionStmt);


        main.getBody().get().getStatements().add(new ExpressionStmt(
                new MethodCallExpr(new NameExpr("arr"), new SimpleName("add"),
                        NodeList.nodeList(new NameExpr("hello world")))
        ));

        System.out.println(compilationUnit);
    }
}
