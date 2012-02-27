package com.attask.jenkins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

/**
 * User: joeljohnson
 * Date: 2/17/12
 * Time: 10:39 AM
 */
@Extension
public class ResultsOverrideRecorderDescriptor extends BuildStepDescriptor<Publisher> {
	public ResultsOverrideRecorderDescriptor() {
		super(ResultsOverrideRecorder.class);
	}

	@Override
	public boolean isApplicable(Class<? extends AbstractProject> jobType) {
		return true;
	}

	@Override
	public String getDisplayName() {
		return "Allow Overriding Build Results";
	}
}