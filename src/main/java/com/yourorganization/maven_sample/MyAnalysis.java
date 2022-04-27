package com.yourorganization.maven_sample;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.yourorganization.maven_sample.printer.CustomDotPrinter;
import com.yourorganization.maven_sample.printer.CustomJsonPrinter;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Some code that uses JavaSymbolSolver.
 */
public class MyAnalysis {

    public static void main(String[] args) throws FileNotFoundException {
        // Set up a minimal type solver that only looks at the classes used to run this sample.
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        String filePath = "./src/main/input/demo.java";

        // Parse some code
        CompilationUnit cu = StaticJavaParser.parse(new File(filePath));
        CustomJsonPrinter jsonPrinter = new CustomJsonPrinter(true);
        CustomDotPrinter dotPrinter = new CustomDotPrinter(true);
        System.out.println(jsonPrinter.output(cu));
        System.out.println("================================");
        System.out.println(dotPrinter.output(cu));


    }
}
