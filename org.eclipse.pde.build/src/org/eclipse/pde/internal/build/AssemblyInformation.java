/**********************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.pde.internal.build;

import java.util.*;
import org.eclipse.core.runtime.model.PluginModel;
import org.eclipse.update.core.IFeature;

public class AssemblyInformation {
	// The list of all the features and plugins to assemble listed on a per config basis 
	//	key: string[] representing the tuple of a config 
	// value: (AssemblyLevelConfigInfo) representing the info for the given config
	private Map assembleInformation = new HashMap(8);

	public AssemblyInformation() {
		// Initialize the content of the assembly information with the configurations 
		for (Iterator iter = AbstractScriptGenerator.getConfigInfos().iterator(); iter.hasNext();) {
			assembleInformation.put((Config) iter.next(), new AssemblyLevelConfigInfo());
		}
	}

	public void addFeature(Config config, IFeature feature) {
		AssemblyLevelConfigInfo entry = (AssemblyLevelConfigInfo) assembleInformation.get(config);
		entry.addFeature(feature);
	}

	public void addPlugin(Config config, PluginModel plugin) {
		AssemblyLevelConfigInfo entry = (AssemblyLevelConfigInfo) assembleInformation.get(config);
		entry.addPlugin(plugin);
	}

	public Collection getPlugins(Config config) {
		return ((AssemblyLevelConfigInfo) assembleInformation.get(config)).getPlugins();
	}

	public void addFragment(Config config, PluginModel fragment) {
		AssemblyLevelConfigInfo entry = (AssemblyLevelConfigInfo) assembleInformation.get(config);
		entry.addFragment(fragment);
	}

	public Collection getFragments(Config config) {
		return ((AssemblyLevelConfigInfo) assembleInformation.get(config)).getFragments();
	}

	public Collection getFeatures(Config config) {
		return ((AssemblyLevelConfigInfo) assembleInformation.get(config)).getFeatures();
	}

	// All the information that will go into the assemble file for a specific info
	private class AssemblyLevelConfigInfo {
		// the plugins that are contained into this config
		private Collection plugins = new HashSet(20);
		//	the fragments that are contained into this config
		private Collection fragments = new HashSet(10);
		// the features that are contained into this config
		private Collection features = new HashSet(7);

		public Collection getFeatures() {
			return features;
		}

		public Collection getPlugins() {
			return plugins;
		}

		public Collection getFragments() {
			return fragments;
		}

		public void addFeature(IFeature feature) {
			features.add(feature);
		}

		public void addPlugin(PluginModel plugin) {
			plugins.add(plugin);
		}

		public void addFragment(PluginModel fragment) {
			fragments.add(fragment);
		}

	}
}
