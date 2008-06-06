/*
 * Copyright 2006 - 2008 Cypal Solutions (tools@cypal.in)
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

package in.cypal.studio.gwt.ui.common;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * @author Prakash G.R.
 * 
 */
public class GwtLabelProvider extends LabelProvider {

	// @Override
	public String getText(Object element) {

		String text;
		if (element == null) {
			text = "";//$NON-NLS-1$
		} else if (element instanceof IFile && Util.isGwtModuleFile((IFile) element)) {
			text = Util.getSimpleName((IFile) element);
		} else {
			text = super.getText(element);
		}
		return text;
	}
}
