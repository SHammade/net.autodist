package net.autodist.DAO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 *  This Class represents the Database Table "Project"
 *  It is used to Implement a History. At the Moment only the newest 
 *  Project in the Database is used by this Class
 * @author Retzlaff, Hammade
 */
public class Project {
	private String name;
	private String packageName;
	private Timestamp date;
	private int id;
	private ArrayList<Method> methods = new ArrayList<Method>();

	public void addMethod(Method method) {
		this.methods.add(method);
	}

	/**
	 * Convenience Method for searching Methods whithin the methods array 
	 * @param methodId
	 * @return the Method with the given Method Id
	 * @throws NoSuchElementException if the Method is not found
	 */
	public Method findMethod(int methodId) {
		for (Method method : methods) {
			if (method.getId() == methodId) {
				return method;
			}
		}
		throw new NoSuchElementException();
	}

	public ArrayList<Method> getMethods() {
		return methods;
	}

	public void setMethods(ArrayList<Method> methods) {
		this.methods = methods;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Timestamp getDate() {
		return this.date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageName() {
		return this.packageName;
	}

	public void removeAllMethods() {
		this.methods = new ArrayList<Method>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + id;
		result = prime * result + ((methods == null) ? 0 : methods.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
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
		Project other = (Project) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (id != other.id)
			return false;
		if (methods == null) {
			if (other.methods != null)
				return false;
		} else if (!methods.equals(other.methods))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}
}