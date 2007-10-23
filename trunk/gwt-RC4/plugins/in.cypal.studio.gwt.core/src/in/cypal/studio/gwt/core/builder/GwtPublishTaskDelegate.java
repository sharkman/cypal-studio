/*
 * Copyright 2006 Cypal Solutions (tools@cypal.in)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package in.cypal.studio.gwt.core.builder;

import in.cypal.studio.gwt.core.common.Constants;
import in.cypal.studio.gwt.core.common.Preferences;
import in.cypal.studio.gwt.core.common.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.PublishOperation;
import org.eclipse.wst.server.core.model.PublishTaskDelegate;


/**
 * @author Prakash G.R.
 * 
 */
public class GwtPublishTaskDelegate extends PublishTaskDelegate {

	public PublishOperation[] getTasks(IServer server, List modulesList) {
		return super.getTasks(server, modulesList);
	}
	
	
	public PublishOperation[] getTasks(IServer server, int kind, List modulesList, List kindList) {

		if(Preferences.getBoolean(Constants.COMPILE_AT_PUBLISH_PREFERENCE, true)
				&& (kind == IServer.PUBLISH_FULL 
						||kind==IServer.PUBLISH_AUTO 
						|| kind== IServer.PUBLISH_INCREMENTAL)) {
			List tasksList = new ArrayList();
			for (Iterator i = modulesList.iterator(); i.hasNext();) {
				IModule[] modules = (IModule[]) i.next();
				for (int j = 0; j < modules.length; j++) {
					IModule module = modules[j];
					IProject project = module.getProject();
					if(Util.hasGwtNature(project)) {
						tasksList.add(new GwtCompileOperation(project));
					}
				}
			}
			return (PublishOperation[]) tasksList.toArray(new PublishOperation[tasksList.size()]);
			
		}else {
			return super.getTasks(server, kind, modulesList, kindList);
		}
	}

}
