package com.attask.jenkins;

import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Collection;

/**
 * User: joeljohnson
 * Date: 2/17/12
 * Time: 10:26 AM
 */
@Deprecated
public class ResultsOverrideRecorder extends Recorder {
	@DataBoundConstructor
	public ResultsOverrideRecorder() {
		//nothing
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		listener.error("Manually enabling '" + getDescriptor().getDisplayName() + "' has been deprecated. Remove it from the job config.");
		return true;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
}
