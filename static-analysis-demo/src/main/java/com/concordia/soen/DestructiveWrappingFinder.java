package com.concordia.soen;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;


public class DestructiveWrappingFinder 
{

	static String delimiter = ";";
	
  public static void main( String[] args )
  {
    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
    for (String filename : args) {
      String source;
      try {
        source = read(filename); 
      } catch (IOException e) {
        System.err.println("can not read file: " + filename);
        continue;
      }

      parser.setSource(source.toCharArray());

      ASTNode root = parser.createAST(null);
      
      String newTree = tree(root);
      
//      System.out.println(newTree);

//      System.out.println(tree(root));
      generateASTString(newTree);
    }
  }

  public static void generateASTString(String newTree) {

//	  System.out.println(newTree);
	  String[] lines = newTree.split("\r\n|\r|\n");
	  
	  int level = 0;
			  
	  String path = delimiter, itemList = delimiter;
			  
	  for (int i = 0; i < lines.length; i++) {
		  String line = lines[i].toString();
		  String cleanline = lines[i].replace("├─", " ");
		  cleanline = lines[i].replace("└─", " ");
		  cleanline = lines[i].replace("│ ", " ");
		  
		  //Get node value
		  String itemName = cleanline.substring(cleanline.indexOf("─") + 1);

		  int new_level = (line.length() - cleanline.replace(" ", "").length()) / 2;
		  
		  
		  //if second number bigger, then update first number to second number, append item 
		  if (new_level > level) {
			  path = path + delimiter + itemName + "(" + new_level + ")";
			  itemList = itemList + delimiter + itemName;
		  }
		  
		  //if second number same as first number, remove last item then append
		  if (new_level == level) {
			
			  int endIndex = path.lastIndexOf(delimiter);
			  int endIndexItemList = itemList.lastIndexOf(delimiter);
			  
//			  System.out.println(path);
			  checkDestructiveWrapper(itemList);
//			  System.out.println(itemList);		
			  
			  if (endIndex != -1)  
			    {
			        path = path.substring(0, endIndex) + delimiter + itemName + "(" + new_level + ")"; // not forgot to put check if(endIndex != -1)
			        itemList = itemList.substring(0, endIndexItemList) + delimiter + itemName;
			        
			    }
		  }

		  //if second number smaller, remove string to item that is -1 of second number, reset number to second number
		  if (new_level < level) {

//			  System.out.println(path);
			  checkDestructiveWrapper(itemList);
//			  System.out.println(itemList);		
			  
			  for(int k = 0; k <= level - new_level; k++) {
				  int endIndex = path.lastIndexOf(delimiter);
				  path = path.substring(0, endIndex);
				  
				  int endIndexItemList = itemList.lastIndexOf(delimiter);
				  itemList = itemList.substring(0, endIndexItemList);
			  }
			  path = path + delimiter + itemName + "(" + new_level + ")";
			  itemList = itemList + delimiter + itemName;
		  }

		  level = new_level;
		  
		  //show the last line
		  if (i == lines.length - 1) {
//			  System.out.println(path);	
			  checkDestructiveWrapper(itemList);
//			  System.out.println(itemList);				  
		  }
	  }
	   
  } 

  //check for destructive wrapper, i.e multiple throws in one string
  public static void checkDestructiveWrapper(String itemList) {
	  String[] array = itemList.split(delimiter);
	  int counter = 0;
	  String path = "";
	  for(int i=0; i < array.length; i++) {
		  if (array[i].toString().contains("TryStatement")) {
			  counter ++;
		  }
	  }
	  
	  //print when more than one trystatement found in a path
	  if (counter > 1) {
		  System.out.println(itemList);
	  }	
	  
  }
  
  public static String read(String filename) throws IOException {
    Path path = Paths.get(filename);

    String source = Files.lines(path).collect(Collectors.joining("\n"));

    return source;
  }

  private static String tree(ASTNode root) {
      TreeBuilder builder = new TreeBuilder();
      root.accept(builder);
      return builder.tree(root);
  } 

  private static class TreeBuilder extends ASTVisitor {
	  
    // mapping from parent to child
    Map<ASTNode, ASTNode> child = new HashMap<>();

    // mapping from node to next sibling
    Map<ASTNode, ASTNode> sibling = new HashMap<>();

    // last visited node
    ASTNode last = null;

    // stack of currently visiting nodes
    Stack<ASTNode> stack = new Stack<>();

    public ASTNode parent(ASTNode node) {
      return node.getParent();
    }

    public ASTNode child(ASTNode node) {
      return child.get(node);
    }

    public ASTNode sibling(ASTNode node) {
      return sibling.get(node);
    }

    @Override
    public void preVisit(ASTNode node) {
      ASTNode parent = node.getParent();

      stack.push(node);

      if (parent != null && child(parent) == null) {
        child.put(parent, node);
      } 

      if (last != null && parent(last) == parent(node)) {
        sibling.put(last, node);
      }
    }

    @Override
    public void postVisit(ASTNode node) {
      last = stack.pop();
    }

    public String repr(ASTNode node) {
      return node.getClass().getSimpleName();
    }

    public String tree(ASTNode root) {
      StringBuilder builder = new StringBuilder();
      builder.append(repr(root));
      builder.append(System.lineSeparator());
      builder.append(tree(child(root), ""));
      return builder.toString();
    }

    public String tree(ASTNode node, String prefix) {
      //System.out.println(1111111);
      if (node == null) {
        return "";
      }

      StringBuilder builder = new StringBuilder();
      builder.append(prefix);
      if (sibling(node) != null) {
        builder.append("├─");
        builder.append(repr(node));
        builder.append(System.lineSeparator());
        builder.append(tree(child(node), prefix + "│ "));
        builder.append(tree(sibling(node), prefix));
        //System.out.println(111);
      } else {
        builder.append("└─");
        builder.append(repr(node));
        builder.append(System.lineSeparator());
        builder.append(tree(child(node), prefix + "  "));
        //System.out.println(111);
      }

      return builder.toString();
    }
  }
}