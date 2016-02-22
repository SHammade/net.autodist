package net.autodist.DAO.database.test;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Test;

import net.autodist.DAO.Annotation;
import net.autodist.DAO.Attribute;
import net.autodist.DAO.Method;
import net.autodist.DAO.Parameter;
import net.autodist.DAO.Project;
import net.autodist.DAO.Thrown_Exception;
import net.autodist.DAO.database.DbConnection;
import net.autodist.DAO.database.DbReader;
import net.autodist.DAO.database.DbWriter;

/**
 * @author Retzlaff, Hammade
 *
 */
public class DatabaseTest {
	
	/**
	 * Complete test of the most important Database 
	 * Functions including writing and Reading a Project including dependencies
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void test() throws SQLException, ClassNotFoundException {
		DbConnection dbcon = new DbConnection();
		assertTrue("Skript executed, tables created", dbcon.createTables());
		dbcon.close();
		DbWriter dbwr = new DbWriter();
		Project pr = new Project();
		pr.setName("TestProject");
		for (int i = 0; i < 10; i++) {
			Method m = new Method();
			m.setProject(pr);
			m.setName("testmethod" + i);
			m.setPath("/hfu/");
			m.setReturn_Type("void");
			m.setSource("public static ...");
			m.setBody("public static ...");
			Annotation a = new Annotation();
			a.setType("myAnno");
			a.addAttribute(new Attribute("mytype", "true"));
			m.addAnnotation(a);
			m.addParameter(new Parameter("String", "myParam"));
			m.addThrown_Exception(new Thrown_Exception("myCoolException"));
			pr.addMethod(m);
		}
		boolean ok = dbwr.writeProjectIncludingDependencies(pr);
		assertTrue("Data Committed:", ok);
		dbwr.close();

		DbReader reader = new DbReader();
		Project proj = reader.readAllDataForFirstProject();
		for (Method method : proj.getMethods()) {
			StringBuffer sb = new StringBuffer();
			sb.append(method.getPath());
			sb.append(method.getName());
			sb.append(", ");
			sb.append(method.getReturn_Type());
			System.out.println(sb.toString());
		}
	}

}
