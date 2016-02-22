package net.autodist.annotationidentifier.parser;

import javax.activation.UnsupportedDataTypeException;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import net.autodist.DAO.Annotation;
import net.autodist.DAO.Method;
import net.autodist.DAO.Parameter;
import net.autodist.DAO.Thrown_Exception;

/**
 * Visit child-nodes of {@link MethodDeclaration} to get information aboute the annotation and the thrown exceptions and let {@link MemberValuePairASTVisitor} analyze the method parameters 
 */
public class MethodDeclarationASTVisitor extends ASTVisitor {

	private Method method;	

	public MethodDeclarationASTVisitor(Method method) {
		this.method = method;
	}

	@Override
	public void preVisit(ASTNode node) {
		if(node instanceof NormalAnnotation)
		{
			NormalAnnotation normalAnnotation = (NormalAnnotation) node;
			Annotation annotation = new Annotation();
			annotation.setType(normalAnnotation.getTypeName().toString());		
			annotation.setMethod(this.method);
			node.accept(new MemberValuePairASTVisitor(annotation));
			this.method.addAnnotation(annotation);			
		}
		else if(node instanceof SingleVariableDeclaration)
		{
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) node;
			if(singleVariableDeclaration.getType().toString().contains("Exception"))
				{return;}
			Parameter parameter = new Parameter();
			parameter.setName(singleVariableDeclaration.getName().toString());
			parameter.setType(getFullType(singleVariableDeclaration.getType()));
			parameter.setMethod(this.method);
			this.method.addParameter(parameter);			
		}
	}

	private String getFullType(Type type) {
		if(type.isSimpleType()){
			String name = ((SimpleType) type).getName().getFullyQualifiedName();
			if(name.equals("ByteBuffer")){
				name = "java.nio.ByteBuffer";
			}
			return name;
		}
		else {
			return type.toString();
		}
	}

}
