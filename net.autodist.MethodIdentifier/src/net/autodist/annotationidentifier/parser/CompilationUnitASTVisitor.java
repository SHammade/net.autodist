package net.autodist.annotationidentifier.parser;

import org.eclipse.jdt.core.dom.*;

import net.autodist.DAO.Project;

/**
 * Visit child-nodes of {@link CompilationUnit} and let {@link PackageDeclarationASTVisitor} and {@link TypeDeclarationASTVisitor} analyze them 
 */
public class CompilationUnitASTVisitor extends ASTVisitor {
	private Project project;
	private PackageDeclarationASTVisitor packageDeclarationASTVisitor;
	private TypeDeclarationASTVisitor typeDeclarationASTVisitor;
		
	public CompilationUnitASTVisitor(Project project) {
		this.project = project;
		this.packageDeclarationASTVisitor = new PackageDeclarationASTVisitor();
		this.typeDeclarationASTVisitor = new TypeDeclarationASTVisitor(this.project);
	}
	
	@Override
	public void preVisit(ASTNode node) {
		if(node instanceof PackageDeclaration)
		{
			node.accept(this.packageDeclarationASTVisitor);
		}
		if(node instanceof TypeDeclaration)
		{
			node.accept(this.typeDeclarationASTVisitor);
		}
	}
		
	public Project getProject() {
		return this.project;
	}

}
