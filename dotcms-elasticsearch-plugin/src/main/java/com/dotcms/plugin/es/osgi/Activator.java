package com.dotcms.plugin.es.osgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;

import com.dotcms.plugin.es.rest.ESContentResourcePortlet;
import com.dotcms.plugin.es.viewtool.EsContentToolInfo;
import com.dotcms.repackage.org.apache.commons.io.FileUtils;
import com.dotcms.repackage.org.apache.commons.io.IOUtils;
import com.dotcms.repackage.org.osgi.framework.BundleContext;
import com.dotcms.rest.config.RestServiceUtil;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.business.Layout;
import com.dotmarketing.common.db.DotConnect;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UUIDGenerator;

public class Activator extends GenericBundleActivator {

	Class clazz = ESContentResourcePortlet.class;

	@SuppressWarnings("unchecked")
	public void start(BundleContext context) throws Exception {

        initializeServices( context );
		
		String jarFile = context.getBundle().getLocation();

        publishBundleServices( context );
		
		// Register REST
		Logger.info(this.getClass(), "Adding new Restful Service:" + clazz.getSimpleName());
		RestServiceUtil.addResource(clazz);

		// Set up ViewTool
		Logger.info(this.getClass(), "Adding new ViewTool:" + EsContentToolInfo.class.getClass().getSimpleName());
		registerViewToolService(context, new EsContentToolInfo());

		// Register our portlet
		String[] xmls = new String[] { "conf/portlet.xml", "conf/liferay-portlet.xml" };
		registerPortlets(context, xmls);



		moveJsps(jarFile.replace("file:", ""));
		putEsMApping(jarFile.replace("file:", ""));
		
		// System layout
  		Layout layout = APILocator.getLayoutAPI().findLayout("a8e430e3-8010-40cf-ade1-5978e61241a8");
		DotConnect db = new DotConnect();
		db.setSQL("delete from cms_layouts_portlets where portlet_id = 'ES_SEARCH_PORTLET'");
		db.loadResult();
		db.setSQL("insert into cms_layouts_portlets (id,  layout_id, portlet_id, portlet_order) values(?,?,?,?)");
		db.addParam(UUIDGenerator.generateUuid());
		db.addParam("a8e430e3-8010-40cf-ade1-5978e61241a8");
		db.addParam("ES_SEARCH_PORTLET");
		db.addParam(10);
		db.loadResult();
		
		CacheLocator.getLayoutCache().clearCache();
		class ReloadRest extends Thread {

		    public void run() {
		    	try {
					Thread.sleep(5000);
					RestServiceUtil.reloadRest();
				} catch (InterruptedException e) {
					throw new DotStateException(e.getMessage());
				}

		    }


		}
		
		
		
		
		
		Thread x = new ReloadRest();
		x.run();
		
	}

	public void stop(BundleContext context) throws Exception {

		Logger.info(this.getClass(), "Removing new Restful Service:" + clazz.getSimpleName());
		RestServiceUtil.removeResource(clazz);
		Logger.info(this.getClass(), "Removing new ViewTool:" + EsContentToolInfo.class.getClass().getSimpleName());

		unregisterServices (  context );
		DotConnect db = new DotConnect();
		db.setSQL("delete from cms_layouts_portlets where portlet_id = 'ES_SEARCH_PORTLET'");
		db.getResult();
		CacheLocator.getLayoutCache().clearCache();

	}

	private void moveJsps(String pathToJar) throws IOException {
		ServletContext context = Config.CONTEXT;
		try {

			String destinationFolder = context.getRealPath("/WEB-INF/jsp");

			JarFile jarFile = new JarFile(pathToJar);

			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();

				if (entry.getName().endsWith(".jsp") && entry.getName().indexOf("jsp/") > -1) {

					String[] filePath = entry.getName().split(File.separator);
					String fileName = filePath[filePath.length - 1];
					String folderPath = destinationFolder;
					for (int i = 1; i < filePath.length - 1; i++) {
						folderPath += "/" + filePath[i];
					}

					new File(folderPath).mkdirs();
					Logger.info(this.getClass(), "found:" + entry.getName());

					InputStream inputStream = jarFile.getInputStream(entry);
					File materializedJsp = new File(folderPath, fileName);
					Logger.info(this.getClass(), "copying to:" + materializedJsp.getAbsolutePath());
					FileOutputStream outputStream = new FileOutputStream(materializedJsp);
					copyAndClose(inputStream, outputStream);
				}
			}

		} catch (MalformedURLException e) {
			throw new FileNotFoundException("Cannot find jar file in libs: " + pathToJar);
		} catch (IOException e) {
			throw new IOException("IOException while moving resources.", e);
		}
	}

	private void putEsMApping(String pathToJar) throws IOException {
		ServletContext context = Config.CONTEXT;
		try {

			JarFile jarFile = new JarFile(pathToJar);

			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();

				if (entry.getName().equals("es-content-mapping.json")) {

					Logger.info(this.getClass(), "found:" + entry.getName());
					InputStream inputStream = jarFile.getInputStream(entry);
					StringWriter writer = new StringWriter();
					IOUtils.copy(inputStream, writer, "UTF8");
					String mapping = writer.toString();


					
					File file =  new File(context.getRealPath("/WEB-INF/classes/es-content-mapping.json"));
					FileUtils.writeByteArrayToFile(file, mapping.getBytes());
					
				}
			}

		} catch (MalformedURLException e) {
			throw new FileNotFoundException("Cannot find jar file in libs: " + pathToJar);
		} catch (IOException e) {
			throw new IOException("IOException while moving resources.", e);
		}
	}

	

	
	
	private static void copyAndClose(InputStream in, OutputStream out) throws IOException {
		try {
			byte[] b = new byte[8192];
			int read;
			while ((read = in.read(b)) != -1) {
				out.write(b, 0, read);
			}
		} finally {
			in.close();
			out.close();
		}
	}

}