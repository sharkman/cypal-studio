/*
 * Copyright 2009 Cypal Solutions (tools@cypal.in)
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

package in.cypal.studio.gwt.core.common;

/**
 * @author Prakash G.R.
 * 
 */
public class GwtRuntime {

	private String name;
	private String location;
	private boolean isWorkspaceDefault;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean isWorkspaceDefault() {
		return isWorkspaceDefault;
	}

	public void setWorkspaceDefault(boolean isWorkspaceDefault) {
		this.isWorkspaceDefault = isWorkspaceDefault;
	}

	@Override
	public String toString() {
		if (isWorkspaceDefault)
			return name + "[default]";
		return name;
	}
}
