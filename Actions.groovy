import java.lang.management.RuntimeMXBean;
import java.lang.management.ManagementFactory;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.Runtime;

import org.json.simple.JSONObject;
import org.codehaus.groovy.tools.shell.CommandAlias;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;


import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;

import org.apache.commons.lang3.StringEscapeUtils

import org.bonitasoft.engine.identity.User;

import org.bonitasoft.web.extension.page.PageContext;
import org.bonitasoft.web.extension.page.PageController;
import org.bonitasoft.web.extension.page.PageResourceProvider;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.BusinessDataAPI;




import org.bonitasoft.page.log.LogAccess;
import org.bonitasoft.page.log.LogInformation;
import org.bonitasoft.page.log.LogItem;
import org.bonitasoft.page.log.LogAccess.LogParameter;
import org.bonitasoft.page.log.LogAccess.LogZip;


public class Actions {

	private static Logger logger= Logger.getLogger("org.bonitasoft.page.log.groovy");


	public static Index.ActionAnswer doAction(HttpServletRequest request, String paramJsonSt, HttpServletResponse response, PageResourceProvider pageResourceProvider, PageContext pageContext) {

		logger.info("#### Actions: Groovy > doAction > Actions start");
		Index.ActionAnswer actionAnswer = new Index.ActionAnswer();
		try {
			String action=request.getParameter("action");
			logger.info("#### Actions: Groovy > doAction > action is["+action+"] !");
			if (action==null || action.length()==0 ) {
				actionAnswer.isManaged=false;
				// logger.fine("#### log:Actions END No Actions");
				return actionAnswer;
			}
			actionAnswer.isManaged=true;

			APISession session = pageContext.getApiSession()
			ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
			IdentityAPI identityApi = TenantAPIAccessor.getIdentityAPI(session);
			CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(session);
			BusinessDataAPI businessDataAPI = TenantAPIAccessor.getBusinessDataAPI(session);


			if ("getFilesLog".equals(action)) {
				logger.info("#### Actions: Groovy > doAction > call getFilesLog");
				//Make sure no action is executed if the CSRF protection is active and the request header is invalid
				if (! TokenValidator.checkCSRFToken(request, response)) {
					actionAnswer.isResponseMap=false;
					return actionAnswer;
				}

				actionAnswer.responseMap.put("listfileslog", LogAccess.getFilesLog());
				actionAnswer.responseMap.put("logpath", LogAccess.getMapLogPath( LogAccess.getLogPath()) );
			}

			else if ("getLog".equals(action)) {
				logger.info("#### Actions: Groovy > doAction > call getLog");
				//Make sure no action is executed if the CSRF protection is active and the request header is invalid
				if (! TokenValidator.checkCSRFToken(request, response)) {
					actionAnswer.isResponseMap=false;
					return actionAnswer;
				}
				
				logger.info("###request.getParameter is: ["+ request.getParameter("log") +"] !");

				String isLog=request.getParameter("log");
				if ("Y".equals(isLog))
					logger.info("###################################### action is["+action+"] !");


				LogParameter logParameters = LogParameter.getInstanceFromJsonSt(paramJsonSt);
				logParameters.produceJson=true;
				if ("Y".equals(isLog))
					logParameters.logInformation=true;

				LogInformation logInformation = LogAccess.getLog(logParameters);

				actionAnswer.responseMap = logInformation.getMap();
			}
			else if ("zipanddownload".equals(action)) {
				logger.info("#### Actions: Groovy > doAction > call zipanddownload");
				// Zip and download is a GET, and a direct access in the HTML page.
				actionAnswer.isManaged=true;
				response.addHeader("content-disposition", "attachment; filename=LogFiles.zip");
				response.addHeader("content-type", "application/zip");

				OutputStream output = response.getOutputStream();
				logger.info("#### log:Actions ZipAndDownload JSON=["+paramJsonSt+"]");

				LogParameter logParameters = LogParameter.getInstanceFromJsonSt(paramJsonSt);
				LogZip logZip = LogAccess.getZipAndDownload(logParameters);

				if (logZip.containerZip!=null)
					logZip.containerZip.writeTo( output );

				output.flush();
				output.close();
				logger.info("#### Actions: Groovy > doAction > zipanddownload *****  responseMap ="+actionAnswer.responseMap.size());
			}
			logger.info("#### Actions: Groovy > doAction > END " );
			return actionAnswer;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			logger.severe("#### log:Groovy Exception in zipanddownload  ["+e.toString()+"] at "+exceptionDetails);
			actionAnswer.isResponseMap=true;
			actionAnswer.responseMap.put("Error", "log:Groovy Exception in zipanddownload ["+e.toString()+"] at "+exceptionDetails);
			return actionAnswer;
		}
	}
}
