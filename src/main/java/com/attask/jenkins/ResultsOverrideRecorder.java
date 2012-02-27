package com.attask.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * User: joeljohnson
 * Date: 2/17/12
 * Time: 10:26 AM
 */
public class ResultsOverrideRecorder extends Recorder {
	@DataBoundConstructor
	public ResultsOverrideRecorder() {
		//nothing
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		build.getActions().add(new ResultsOverrideAction(build));
		return true;
	}

//	@Override
//	public Action getProjectAction(AbstractProject<?,?> project) {
//		Action action = null;
//		if (project.getLastBuild() != null) {
//			action = new ResultsOverrideAction();
//		}
//		return action;
//	}


	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
}
