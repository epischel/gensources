package de.epischel.gensources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class Generator {
	
	static Properties properties = new Properties();
	static Properties htmls = new Properties();
	static PrintStream ps = System.out;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		properties.load(new FileInputStream("src/main/resources/sources.properties"));
		htmls.load(new FileInputStream("src/main/resources/html.properties"));
		ps = new PrintStream(new File("build/gensources.html"),"UTF-8");
		
		List<String> quellen = properties.keySet().stream()
				.map(s->(String)s).filter(s->s.startsWith("quelle")).collect(Collectors.toList());
		
		ps.println("<!DOCTYPE html>");
		ps.println("<html charset=UTF-8>");
		ps.println(htmls.getProperty("head"));
		ps.println("  <body>");
		ps.println(htmls.getProperty("headline"));
		ps.println(htmls.getProperty("form.start"));
		
		quellen.forEach(q -> printList(q, ps));
		
		ps.println(htmls.getProperty("form.selectend"));
		printInputFields(ps);
		ps.println(htmls.getProperty("form.end"));
		
		ps.println("  <script>");
		ps.println(htmls.getProperty("js.templatearray"));
		quellen.forEach(q -> printArray(q,ps));
		ps.println(htmls.getProperty("js.birthreq"));
		printApplyTemplate(ps);
		ps.println("}");
		ps.println(htmls.getProperty("js.onclick"));
		ps.println(htmls.getProperty("js.datumdeutsch"));
		ps.println(htmls.getProperty("js.format"));
		ps.println("  </script>");
		ps.println("  </body>");
		ps.println("</html>");
	}
	
	private static void printArray(String quelle, PrintStream os) {
		String quelleValue = properties.getProperty(quelle);
		os.printf("templates['%s'] = '%s';%n", quelleValue, properties.getProperty("template."+quelleValue));
		
	}

	static void printList(String quelle, PrintStream os) {
		String quelleValue = properties.getProperty(quelle);
		os.printf("    <option value='%s'>%s</option>%n", quelleValue, properties.getProperty("optiontext."+quelleValue));
	}
	
	static void printInputFields(PrintStream os) {
		List<String> labels = getLabels();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
		String today = sdf.format(new Date());
		os.println("    <label for=\"visited\">Besucht am :</label> "
				+ "<input type=\"date\" name=\"visited\" id=\"visited\" value=\""+today+"\"/><br>");
		
		labels.stream().forEach(feld -> printInputField(feld, os));
		
	}

	private static List<String> getLabels() {
		List<String> labels = properties.keySet().stream()
				.map(s->(String)s).filter(s->s.startsWith("label.")).map(s->s.substring(6)).collect(Collectors.toList());
		return labels;
	}
	
	static void printInputField(String feld, PrintStream os) {
		String typeOfFeld = properties.getProperty("type."+feld, "text");
		os.printf("    <label for='%s'>%s :</label> <input name='%s' type='%s' id='%s'><br> %n", 
				feld, properties.getProperty("label."+feld), feld, typeOfFeld, feld);
	}
	
	static void printApplyTemplate(PrintStream os) {
		List<String> labels = getLabels();
		labels.add("visited");
		List<String> argumenList = labels.stream()
				.map(l->"\""+l+"\":document.getElementById(\""+l+"\").value").collect(Collectors.toList());
		String argString = String.join(",", argumenList);
		System.out.println(argString);;
		os.println("document.getElementById(\"birthregout\").value =");
		os.println("template.formatUnicorn({"+argString+"})");
	}
}
