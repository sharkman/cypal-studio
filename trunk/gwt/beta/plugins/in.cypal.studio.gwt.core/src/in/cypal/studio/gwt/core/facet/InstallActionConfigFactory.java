/*
 * Copyright 2006 Prakash (techieguy@gmail.com)
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

package in.cypal.studio.gwt.core.facet;

import in.cypal.studio.gwt.core.common.Constants;

import org.eclipse.wst.common.componentcore.datamodel.FacetInstallDataModelProvider;


/**
 * @author Prakash (techieguy@gmail.com)
 *
 */
public class InstallActionConfigFactory extends FacetInstallDataModelProvider{

	public Object getDefaultProperty(String propertyName) {

		Object property;
		if (propertyName.equals(FACET_ID)) {
			property =  Constants.FACET_ID;
		}else {
			property = super.getDefaultProperty(propertyName);
		}
		
		return property;
	}
	

}
