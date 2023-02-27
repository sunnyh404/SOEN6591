package com.concordia.soen;

import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.FileWriter;


public class DestructiveWrappingFinder 
{

	public static final File[] files = listFiles("C:\\Users\\Heng\\6591\\static-analysis-demo");

	static final File outputFile = new File("C:\\Users\\Heng\\6591\\static-analysis-demo\\DestructiveWrapper.txt");

	
        
	  public static void main(String[]  args) {
	    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());

	    if (outputFile.exists()) {
	    	outputFile.delete();}
	    
	    try {
			outputFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		

		for (File file : files) {
		    System.out.println(file.getAbsolutePath());
		    String source;
	  	      try {
	  	        source = StaticAnalysisDemo.read(file.toString());
	  	      } catch (IOException e) {
	  	        System.err.println(e);
	  	        continue;
	  	      }

	  	      parser.setSource(source.toCharArray());

	  	      parser.setKind(ASTParser.K_COMPILATION_UNIT); // set the kind of AST to be parsed

	  	      CompilationUnit cu = (CompilationUnit) parser.createAST(null); // parse the source code into a CompilationUnit
	  		    
	  	      cu.accept(new Visitor(file, cu));
	  	      
		}

	  }

	        
	  static class Visitor extends ASTVisitor {

		  private CompilationUnit cu;
		  private File file;
		    
		  public Visitor(File file, CompilationUnit cu) {
		        this.file = file;
		        this.cu = cu;
		    }
		  
			public boolean visit(MethodDeclaration method) {
				if (method.getBody() != null) {

					method.getBody().accept(new ASTVisitor() {
						
						@Override
						public boolean visit(CatchClause clause) {

							if (clause.getBody().statements().isEmpty() == false) {
								
								
								clause.getBody().accept(new ASTVisitor() {

									public boolean visit(ThrowStatement node) {
										int start = node.getStartPosition();
										int lineNumber = cu.getLineNumber(start);		


										//System.out.println("Empty Catch Block:" + callsite + " :" + lineNumber);

																			
										String str = "<filepath>" + file
												+ "</filepath>" + "<line>" + lineNumber + "</line>\r\n";
										appendToFile(outputFile, str);
										
										
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
	  
	  //get list of files from folder
	  public static File[] listFiles(String folderPath) {
	        File folder = new File(folderPath);
	        List<File> fileList = new ArrayList<File>();

	        addFilesToList(folder, fileList);

	        return fileList.toArray(new File[fileList.size()]);
	    }

	    private static void addFilesToList(File folder, List<File> fileList) {
	        File[] files = folder.listFiles();

	        for (File file : files) {
	            if (file.isDirectory()) {
	                addFilesToList(file, fileList);
	            } else {
	            	if (file.toString().endsWith(".java")){
	                fileList.add(file);}
	            }
	        }
	    }
	    
	    public static void appendToFile(File filePath, String content) {
	        try {
	            FileWriter writer = new FileWriter(filePath, true);
	            writer.write(content);
	            writer.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    
	  
	}
