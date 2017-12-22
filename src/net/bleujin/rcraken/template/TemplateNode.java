package net.bleujin.rcraken.template;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.script.StringInputStream;
import net.ion.framework.mte.Engine;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.StringUtil;

public class TemplateNode {

	private final TemplateFac tfac;
	private final ReadSession rsession;
	private final Fqn fqn;
	
	private final String templateName;
	private ReadNode templateNode ;
	private ParamMap params = new ParamMap(MapUtil.EMPTY);

	TemplateNode(TemplateFac tfac, ReadSession rsession, Fqn fqn, String templateName) {
		this.tfac = tfac ;
		this.rsession = rsession ;
		this.fqn = fqn ;
		this.templateName = StringUtil.defaultString(templateName, "") ;
	}

	public TemplateNode parameters(String query) {
		this.params = ParamMap.create(query) ;
		return this;
	}

	public ReadNode targetNode() {
		return rsession.pathBy(fqn) ;
	}

	public ReadNode templateNode() {
		if (templateNode == null) {
//			templateNode = findTemplateNode() ;
		}
		return null ;
	}

	public StringBuilder template() {
		return null ;
	}

	public String templateName() {
		return templateName;
	}

	public void transform(Writer writer) {
		try {
			Engine engine = rsession.workspace().parseEngine();
			String transformed = engine.transform(findTemplate(), MapUtil.<String, Object>chainMap().put("self", targetNode()).put("params", params).toMap()) ;
			IOUtil.copy(new StringReader(transformed), writer) ;
		} catch(IOException ex) {
			throw new IllegalStateException(ex) ;
		}
	}
	
	private String findTemplate() {
		ReadNode current = targetNode() ;
		while(! current.isRoot()) {
			if (current.hasProperty(templateName)) {
				return current.asString(templateName) ;
			} else if (templateName.isEmpty() && current.hasProperty("template")) {
				return current.asString("template") ;
			} if (current.hasRef(templateName) && current.ref(templateName).hasProperty("template")) {
				return current.ref(templateName).asString("template") ;
			}
			current = current.parent() ;
		}
		return tfac.findTemplate(templateName) ;
	}

}
