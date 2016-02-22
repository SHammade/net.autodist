package net.autodist.annotationidentifier.parser;

import org.eclipse.jdt.core.dom.*;

/**
 * Visit child-nodes of {@link PackageDeclaration} to get the packagename
 */
public class PackageDeclarationASTVisitor extends ASTVisitor {
	private String packageName;

	@Override
	public void preVisit(ASTNode node) {
		if(node instanceof QualifiedName)
			this.packageName = ((QualifiedName) node).getFullyQualifiedName();		
	}
	
	public String getPackageName() {
		return this.packageName;
	}
}
