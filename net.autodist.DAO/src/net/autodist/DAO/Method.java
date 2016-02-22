package net.autodist.DAO;

import java.util.ArrayList;

/**
 * This Class represents the Database Table "Method"
 * @author Retzlaff, Hammade
 */
public class Method {
	private String path;
	private String name;
	private int id;
	private String return_Type;
	private String source;
	private String body;
	private Project project;

	private ArrayList<Parameter> parameters = new ArrayList<Parameter>();
	private ArrayList<Thrown_Exception> thrown_Exceptions = new ArrayList<Thrown_Exception>();
	private ArrayList<Annotation> annotations = new ArrayList<Annotation>();

	public void addParameter(Parameter parameter) {
		parameter.setMethod(this);
		this.parameters.add(parameter);
	}

	public void addThrown_Exception(Thrown_Exception thrownException) {
		thrownException.setMethod(this);
		this.thrown_Exceptions.add(thrownException);
	}

	public void addAnnotation(Annotation annotation) {
		annotation.setMethod(this);
		this.annotations.add(annotation);
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getReturn_Type() {
		return this.return_Type;
	}

	public void setReturn_Type(String return_Type) {
		this.return_Type = return_Type;
	}

	public String getSource() {
		return this.source;
	}

	public void setSource(String completeMethod) {
		this.source = completeMethod;
	}

	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public ArrayList<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(ArrayList<Parameter> parameters) {
		this.parameters = parameters;
	}

	public ArrayList<Thrown_Exception> getThrown_Exceptions() {
		return thrown_Exceptions;
	}

	public void setThrown_Exceptions(ArrayList<Thrown_Exception> thrown_Exceptions) {
		this.thrown_Exceptions = thrown_Exceptions;
	}

	public ArrayList<Annotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(ArrayList<Annotation> annotations) {
		this.annotations = annotations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		result = prime * result + ((return_Type == null) ? 0 : return_Type.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((thrown_Exceptions == null) ? 0 : thrown_Exceptions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Method other = (Method) obj;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		if (return_Type == null) {
			if (other.return_Type != null)
				return false;
		} else if (!return_Type.equals(other.return_Type))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (thrown_Exceptions == null) {
			if (other.thrown_Exceptions != null)
				return false;
		} else if (!thrown_Exceptions.equals(other.thrown_Exceptions))
			return false;
		return true;
	}
}