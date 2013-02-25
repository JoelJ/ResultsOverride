package com.attask.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.*;
import hudson.tasks.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * User: joeljohnson
 * Date: 2/17/12
 * Time: 10:26 AM
 */
public class ResultsOverrideRecorder extends Recorder implements MatrixAggregatable {
	@DataBoundConstructor
	public ResultsOverrideRecorder() {
		//nothing
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		build.addAction(new ResultsOverrideAction(build));
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

	public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
		return new MatrixAggregator(build, launcher, listener) {
			@Override
			public boolean startBuild() throws InterruptedException, IOException {
				return perform(build, launcher, listener);
			}
		};
	}
}
