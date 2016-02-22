package net.autodist.annotationidentifier.parser;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import net.autodist.DAO.Method;
import net.autodist.DAO.Project;
import net.autodist.DAO.Thrown_Exception;

/**
 * Visit child-nodes of {@link TypeDeclaration} to get information about the method and let {@link MethodDeclarationASTVisitor} analyze the annotation, thrown exceptions and method parameter 
 */
public class TypeDeclarationASTVisitor extends ASTVisitor {
	private Project project;
	
	public TypeDeclarationASTVisitor(Project project) {
		this.project = project;
	}
	
	@Override
	public void preVisit(ASTNode node) {
		if(node instanceof MethodDeclaration)
		{
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			if(methodDeclaration.isConstructor())
				return;
			
			Block block = methodDeclaration.getBody();
			Method method = new Method();	
			method.setName(methodDeclaration.getName().toString());
			method.setBody(block.toString());	
			
			String returnTypeName = methodDeclaration.getReturnType2().toString(); 
			if(returnTypeName.equals("ByteBuffer")){
				returnTypeName = "java.nio.ByteBuffer";
			}
			
			method.setReturn_Type(returnTypeName);
			StringBuffer fullName = new StringBuffer();
			if(methodDeclaration.getParent() instanceof TypeDeclaration)
			{
				TypeDeclaration typeDeclaration = (TypeDeclaration) methodDeclaration.getParent();				
				if(typeDeclaration.getParent() instanceof PackageDeclaration)
				{
					PackageDeclaration packageDeclaration = (PackageDeclaration) typeDeclaration.getParent();
					fullName.append(packageDeclaration.getName().getFullyQualifiedName());
					fullName.append('.');
				}
				fullName.append(typeDeclaration.getName().toString());
			}			
			method.setPath(fullName.toString());
			method.setSource(methodDeclaration.toString());
			method.setProject(this.project);	
			methodDeclaration.accept(new MethodDeclarationASTVisitor(method));
						
			if(method.getAnnotations().size() > 0)
				this.project.addMethod(method);		
		}
	}
}
