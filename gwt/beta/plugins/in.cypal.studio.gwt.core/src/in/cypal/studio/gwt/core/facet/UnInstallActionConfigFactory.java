package in.cypal.studio.gwt.core.facet;

import in.cypal.studio.gwt.core.common.Constants;

import org.eclipse.wst.common.componentcore.datamodel.FacetInstallDataModelProvider;

public class UnInstallActionConfigFactory extends FacetInstallDataModelProvider {

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
