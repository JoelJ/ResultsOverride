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

	public boolean checkIsUserAuthenticated() {
		boolean result = build.hasPermission(AbstractBuild.UPDATE);
		return result;
	}
	
	public User getLastEditBy() {
		return lastEditBy;
	}
	
	public Date getLastEditOn() {
		return lastEditOn;
	}

	public void doChangeStatus(StaplerRequest request, StaplerResponse response) throws ServletException, IOException {
		boolean isAuthenticated = checkIsUserAuthenticated();
		boolean isBuilding = build.isBuilding();
		if(isAuthenticated && !isBuilding) {
			String status = request.getParameter("status");
			Result result = Result.fromString(status);
			updateStatus(result);
		} else {
			LOGGER.info("Skipping change status event: authenticated? " + isAuthenticated + " building? " + isBuilding);
		}
		response.forwardToPreviousPage(request);
	}

	/**
	 * Updates the this.build instance's result to the given result.
	 * If this.build is a MatrixBuild or MatrixRun, it also updates the parent/children to have the correct status as well.
	 * Also updates the lastEditBy and lastEditOn fields.
	 */
	private void updateStatus(Result result) throws IOException {
		if(build instanceof MatrixBuild) {
			updateMatrixChildren(result, (MatrixBuild) build);
		}

		forceChangeStatus(build, result);

		if(build instanceof MatrixRun) {
			updateMatrixParentToWorst(result, ((MatrixRun) build).getParentBuild());
		}

		lastEditBy = User.current();
		if(lastEditBy == null) {
			lastEditBy = User.getUnknown();
		}
		lastEditOn = new Date();
		LOGGER.info(lastEditBy.getFullName() + " (" + lastEditBy.getId() + ") edited the status on " + lastEditOn.toString() + " from " + build.getResult() + " to " + result);

		this.save();
	}

	/**
	 * Updates all the child MatrixRuns of the given matrix build to the given result *if* it's better that their individual results.
	 */
	private void updateMatrixChildren(Result result, MatrixBuild matrixbuild) throws IOException {
		for (MatrixRun matrixRun : matrixbuild.getRuns()) {
			if(matrixRun.getNumber() == build.getNumber() && result.isBetterThan(matrixRun.getResult())) {
				forceChangeStatus(matrixRun, result);
				matrixRun.save();
			}
		}
	}

	/**
	 * Updates the given matrix build's result to be the worst of all it's children.
	 */
	private void updateMatrixParentToWorst(Result result, MatrixBuild parentBuild) {
		if(!parentBuild.isBuilding()) {
			Result worst = result;
			for (MatrixRun matrixRun : parentBuild.getRuns()) {
				if(matrixRun.getNumber() == build.getNumber() &&
						matrixRun.getResult().isWorseThan(worst)) {
					worst = matrixRun.getResult();
				}
			}
			forceChangeStatus(parentBuild, result);
		}
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
