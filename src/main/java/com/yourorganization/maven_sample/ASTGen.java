package com.yourorganization.maven_sample;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yourorganization.maven_sample.printer.CustomDotPrinter;
import com.yourorganization.maven_sample.printer.CustomJsonPrinter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class ASTGen {

    public static int[] visit(Node node, JsonObject jsonObject, JsonArray result,int[] nodeId) {
        // [0] nodeId, [1] parentId, [2] chainId, [3] blockId, [4] parentBlockId
        if (node == null) {
            return nodeId;
        }
        // get node info
        final NodeMetaModel metaModel = node.getMetaModel();
        final List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();
        final List<PropertyMetaModel> attributes = allPropertyMetaModels.stream().filter(PropertyMetaModel::isAttribute).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subNodes = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNode).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subLists = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNodeList).collect(toList());

        String NodeType = metaModel.getTypeName();

        // check whether node has attribute
        if (attributes.size() == 0) {
            nodeId[1] = nodeId[0];
        }
        // check whether NodeType is in [ClassOrInterfaceDeclaration, BlockStmt,MethodCallExpr], and increase blockId by 1
        if (NodeType.equals("ClassOrInterfaceDeclaration") || NodeType.equals("BlockStmt") || NodeType.equals("MethodCallExpr")) {
            nodeId[4] = nodeId[3];
            nodeId[3]++;
        }


        jsonObject.addProperty("NodeType", NodeType);
        jsonObject.addProperty("parentId", nodeId[1]);
        jsonObject.addProperty("chainId", nodeId[2]);
        jsonObject.addProperty("nodeId", nodeId[0]);
        jsonObject.addProperty("blockId", nodeId[3]);
        jsonObject.addProperty("parentBlockId", nodeId[4]);
        for (final PropertyMetaModel attributeMetaModel : attributes) {
            jsonObject.addProperty("name", attributeMetaModel.getName());
            jsonObject.addProperty("type", attributeMetaModel.getType().getSimpleName());
            jsonObject.addProperty("value", attributeMetaModel.getValue(node).toString());

        }
        result.add(jsonObject);
        // add subNodes
        for (final PropertyMetaModel subNodeMetaModel : subNodes) {
            final Node subNode = (Node) subNodeMetaModel.getValue(node);
            if (subNode != null) {
                JsonObject subNodeObject = new JsonObject();
                subNodeObject.addProperty("name", subNodeMetaModel.getName());
                nodeId[2] = nodeId[0];
                nodeId[0]++;
                nodeId = visit(subNode, subNodeObject, result,nodeId);

            }
        }

        for (final PropertyMetaModel subListMetaModel : subLists) {
            final NodeList<? extends Node> subList = (NodeList<? extends Node>) subListMetaModel.getValue(node);
            if (subList != null) {
                for (final Node subNode : subList) {
                    JsonObject subNodeObject = new JsonObject();
                    subNodeObject.addProperty("name", subListMetaModel.getName());
                    nodeId[2] = nodeId[0];
                    nodeId[0]++;
                    nodeId = visit(subNode, subNodeObject, result, nodeId);
                }
            }
        }
        return nodeId;
    }


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

        JsonArray jsonArray = new JsonArray();
        // [0] nodeId, [1] parentId, [2] chainId, [3] blockId, [4] parentBlockId
        int[] nodeId = new int[5];
        visit(cu,  new JsonObject(), jsonArray,nodeId);
        System.out.println(jsonArray);

    }

}