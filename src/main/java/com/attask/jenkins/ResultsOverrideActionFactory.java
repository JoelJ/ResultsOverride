package com.attask.jenkins;

import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Joel Johnson
 * Date: 2/25/13
 * Time: 1:31 PM
 */
@Extension
public class ResultsOverrideActionFactory extends RunListener<AbstractBuild> {
	@Override
	public void onCompleted(AbstractBuild abstractBuild, TaskListener listener) {
		if(abstractBuild.getAction(ResultsOverrideAction.class) == null) {
			ResultsOverrideAction action = new ResultsOverrideAction(abstractBuild);
			abstractBuild.addAction(action);
			listener.getLogger().println("Adding Build Override");
		}
	}
}
