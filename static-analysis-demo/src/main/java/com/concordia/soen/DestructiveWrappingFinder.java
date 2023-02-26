package com.concordia.soen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import org.eclipse.jdt.core.dom.ThrowStatement;

import javax.swing.plaf.basic.BasicTreeUI.TreeCancelEditingAction;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.concordia.soen.MethodDeclarationCounter.Visitor;


public class DestructiveWrappingFinder 
{

	  public static void main(String[]  args) {
	    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());

	    for (String filename : args) {
	      String source;
	      try {
	        source = StaticAnalysisDemo.read(filename);
	      } catch (IOException e) {
	        System.err.println(e);
	        continue;
	      }

	      parser.setSource(source.toCharArray());
	    
	      ASTNode root = parser.createAST(null);

	      root.accept(new Visitor());
	    }
	    
	  }


	  static class Visitor extends ASTVisitor {

			public boolean visit(MethodDeclaration method) {

				if (method.getBody() != null) {
					method.getBody().accept(new ASTVisitor() {
						
						@Override
						public boolean visit(CatchClause clause) {

							if (clause.getBody().statements().isEmpty() == false) {
								clause.getBody().accept(new ASTVisitor() {

									public boolean visit(ThrowStatement node) {
								        System.out.println("Caught a throw statement: " + node.getExpression().toString());
								        return super.visit(node);
								    }
								
									
							});

							}

							return true;

						}
					});

				}

				return true;
			}
			
	  }
	  
	}
