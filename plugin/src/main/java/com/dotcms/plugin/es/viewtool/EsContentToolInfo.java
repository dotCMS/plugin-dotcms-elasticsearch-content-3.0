package com.dotcms.plugin.es.viewtool;

import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.servlet.ServletToolInfo;

public class EsContentToolInfo extends ServletToolInfo {

	@Override
	public String getKey() {
		return "estool";
	}

	@Override
	public String getScope() {
		return ViewContext.REQUEST;
	}

	@Override
	public String getClassname() {
		return EsContentTool.class.getName();
	}

	@Override
	public Object getInstance(Object initData) {

		EsContentTool viewTool = new EsContentTool();
		viewTool.init(initData);

		setScope(ViewContext.REQUEST);

		return viewTool;
	}

}