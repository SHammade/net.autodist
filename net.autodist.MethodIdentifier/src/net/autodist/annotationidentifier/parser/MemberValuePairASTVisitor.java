package net.autodist.annotationidentifier.parser;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MemberValuePair;

import net.autodist.DAO.Annotation;
import net.autodist.DAO.Attribute;

/**
 * Visit child-nodes of {@link MemberValuePair} to get the method parameter 
 */
public class MemberValuePairASTVisitor extends ASTVisitor{

	private Annotation annotation;

	public MemberValuePairASTVisitor(Annotation annotation) {
		this.annotation = annotation;
	}
	
	@Override
	public void preVisit(ASTNode node) {
		if(node instanceof MemberValuePair)
		{
			MemberValuePair memberValuePair = (MemberValuePair) node;			
			Attribute attribute = new Attribute(memberValuePair.getName().toString(), memberValuePair.getValue().toString().replaceAll("\"", ""));		
			annotation.addAttribute(attribute);			
		}
	}
	
}
