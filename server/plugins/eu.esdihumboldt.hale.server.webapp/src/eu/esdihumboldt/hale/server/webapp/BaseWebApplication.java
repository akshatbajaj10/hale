/*
 * Copyright (c) 2012 Data Harmonisation Panel
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
 *     HUMBOLDT EU Integrated Project #030962
 *     Data Harmonisation Panel <http://www.dhpanel.eu>
 */

package eu.esdihumboldt.hale.server.webapp;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.esdihumboldt.hale.server.security.UserConstants;
import eu.esdihumboldt.hale.server.webapp.pages.ExceptionPage;
import eu.esdihumboldt.hale.server.webapp.pages.LoginPage;
import eu.esdihumboldt.hale.server.webapp.pages.SecuredPage;
import eu.esdihumboldt.hale.server.webapp.util.DynamicSpringComponentInjector;

/**
 * A basic class for web applications
 * 
 * @author Michel Kraemer
 * @author Simon Templer
 */
public abstract class BaseWebApplication extends WebApplication {

	/**
	 * The default title of a web application
	 */
	public static final String DEFAULT_TITLE = "HALE Web";

	/**
	 * Name of the system property that allows to specify a custom main title.
	 */
	public static final String SYSTEM_PROPERTY_MAIN_TITLE = "hale.webapp.maintitle";

	/**
	 * Get the default application title. Is either the value of the system
	 * property {@value #SYSTEM_PROPERTY_MAIN_TITLE} or {@link #DEFAULT_TITLE}.
	 * 
	 * @return the default title
	 */
	public static String getDefaulTitle() {
		return System.getProperty(SYSTEM_PROPERTY_MAIN_TITLE, DEFAULT_TITLE);
	}

	/**
	 * @return the main title of this application
	 */
	public String getMainTitle() {
		return getDefaulTitle();
	}

	@Override
	public void init() {
		super.init();

		// enforce mounts so security interceptors based on URLs can't be fooled
		getSecuritySettings().setEnforceMounts(true);

		getSecuritySettings().setAuthorizationStrategy(
				new SimplePageAuthorizationStrategy(SecuredPage.class, LoginPage.class) {

					@Override
					protected boolean isAuthorized() {
						SecurityContext securityContext = SecurityContextHolder.getContext();
						if (securityContext != null) {
							Authentication authentication = securityContext.getAuthentication();
							if (authentication != null && authentication.isAuthenticated()) {
								for (GrantedAuthority authority : authentication.getAuthorities()) {
									if (authority.getAuthority().equals(UserConstants.ROLE_USER)
											|| authority.getAuthority().equals(
													UserConstants.ROLE_ADMIN)) {

										// allow access only for users/admins
										return true;
									}
								}
							}
						}

						return false;
					}

				});

		addComponentInstantiationListener(new DynamicSpringComponentInjector());

		// add login page to every application based on this one
		// XXX make configurable?

		// login page
		mountBookmarkablePage("/login", LoginPage.class);
	}

	/**
	 * @see WebApplication#newRequestCycle(Request, Response)
	 */
	@Override
	public RequestCycle newRequestCycle(Request request, Response response) {
		return new WebRequestCycle(this, (WebRequest) request, (WebResponse) response) {

			@Override
			public Page onRuntimeException(Page page, RuntimeException e) {
				return new ExceptionPage(e);
			}

		};
	}

}