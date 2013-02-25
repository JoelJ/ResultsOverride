package com.attask.jenkins;

import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.*;
import hudson.security.Permission;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.logging.Logger;

/**
 * User: joeljohnson
 * Date: 2/16/12
 * Time: 3:57 PM
 */
public class ResultsOverrideAction implements Action, Saveable {
	private static final Logger LOGGER = Logger.getLogger("ResultsOverride");
	private final AbstractBuild<?, ?> build;
	
	private User lastEditBy;
	private Date lastEditOn;

	public ResultsOverrideAction(AbstractBuild<?, ?> build) {
		if(build == null) throw new RuntimeException("build cannot be null");
		this.build = build;
	}

	public boolean getIsUserAuthenticated() {
		User user = User.current();
		return user != null && User.current().hasPermission(Permission.CONFIGURE);
	}
	
	public User getLastEditBy() {
		return lastEditBy;
	}
	
	public Date getLastEditOn() {
		return lastEditOn;
	}

	public void doChangeStatus(StaplerRequest request, StaplerResponse response) throws ServletException, IOException {
		boolean isAuthenticated = getIsUserAuthenticated();
		boolean isBuilding = build.isBuilding();
		if(isAuthenticated && !isBuilding) {
			String status = request.getParameter("status");
			Result result = Result.fromString(status);
			if(build instanceof MatrixBuild) {
				for (MatrixRun matrixRun : ((MatrixBuild) build).getRuns()) {
					if(matrixRun.getNumber() == build.getNumber() && result.isBetterThan(matrixRun.getResult())) {
						forceChangeStatus(build, result);
						matrixRun.save();
					}
				}
			} else {
				forceChangeStatus(build, result);
			}
			this.save();

			lastEditBy = User.current();
			lastEditOn = new Date();
			LOGGER.info(lastEditBy.getFullName() + " (" + lastEditBy.getId() + ") edited the status on " + lastEditOn.toString() + " from " + build.getResult() + " to " + result);
		} else {
			LOGGER.info("Skipping change status event: authenticated? " + isAuthenticated + " building? " + isBuilding);
		}
		response.forwardToPreviousPage(request);
	}

	public AbstractBuild getBuild() {
		return build;
	}

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return "Override Build Status";
	}

	public String getUrlName() {
		return "resultsOverride";
	}

	public void save() throws IOException {
		build.save();
	}

	/**
	 * Uses reflection to change the status of the build associated with this Action.
	 * It uses reflection since Build#setResult only allows the Result to change
	 * if the new result is worse. So we must manually set the field via reflection.
	 * @param status The Result to change the result of this.build.
	 */
	private static void forceChangeStatus(AbstractBuild build, Result status) {
		try {
			Field resultField = Run.class.getDeclaredField("result");
			resultField.setAccessible(true);
			resultField.set(build, status);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
