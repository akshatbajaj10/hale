/*
 * Copyright (c) 2017 wetransform GmbH
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     wetransform GmbH <http://www.wetransform.to>
 */

package eu.esdihumboldt.hale.ui.service.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.ui.PlatformUI;

import com.ibm.icu.text.MessageFormat;

import eu.esdihumboldt.hale.common.core.io.Value;
import eu.esdihumboldt.hale.common.core.io.ValueProperties;
import eu.esdihumboldt.hale.common.core.io.project.ProjectVariables;

/**
 * This class provides content proposals for project variables by loading them
 * via the {@link ProjectService}.<br>
 * <br>
 * <b>Note:</b> After instantiation the result of
 * {@link #getProposals(String, int)} does not change until {@link #reload()} is
 * called.
 * 
 * @author Florian Esser
 */
public class ProjectVariablesContentProposalProvider implements IContentProposalProvider {

	private final Map<String, Value> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * Creates the content proposal provider and loads the project variables
	 */
	public ProjectVariablesContentProposalProvider() {
		reload();
	}

	/**
	 * @see org.eclipse.jface.fieldassist.IContentProposalProvider#getProposals(java.lang.String,
	 *      int)
	 */
	@Override
	public IContentProposal[] getProposals(final String contents, final int position) {
		List<IContentProposal> proposals = new ArrayList<>();
		if (variables != null) {
			variables.forEach((varName, varValue) -> proposals.add(new IContentProposal() {

				@Override
				public String getLabel() {
					return varName;
				}

				@Override
				public String getDescription() {
					return MessageFormat.format("{0} -> \"{1}\"", varName, varValue);
				}

				@Override
				public int getCursorPosition() {
					return getContent().length();
				}

				@Override
				public String getContent() {
					StringBuilder content = new StringBuilder();
					if (contents != null && !contents.substring(0, position).endsWith("{")) {
						content.append("{");
					}
					content.append("{project:").append(varName).append("}}");

					return content.toString();
				}
			}));
		}

		return proposals.toArray(new IContentProposal[proposals.size()]);
	}

	/**
	 * Refresh the project variables from the {@link ProjectService}
	 */
	public void reload() {
		ProjectService ps = PlatformUI.getWorkbench().getService(ProjectService.class);
		Value value = ps.getConfigurationService()
				.getProperty(ProjectVariables.PROJECT_PROPERTY_VARIABLES);
		ValueProperties properties = value.as(ValueProperties.class);

		variables.clear();
		if (properties != null) {
			variables.putAll(properties);
		}
	}
}
