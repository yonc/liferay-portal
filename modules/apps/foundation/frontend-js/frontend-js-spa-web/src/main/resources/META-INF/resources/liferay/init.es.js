'use strict';

import ActionURLScreen from './screen/ActionURLScreen.es';
import App from './app/App.es';
import async from 'metal/src/async/async';
import globals from 'senna/src/globals/globals';
import RenderURLScreen from './screen/RenderURLScreen.es';
import Uri from 'metal-uri/src/Uri';
import utils from 'senna/src/utils/utils';
import {match} from 'metal-dom';

/**
 * Initializes a Senna App with routes that match both ActionURLs and RenderURLs.
 * It also overrides Liferay's default Liferay.Util.submitForm to makes sure
 * forms are properly submitted using SPA.
 * @return {!App} The Senna App initialized.
 * @review
 */
var initSPA = function() {
	let app = new App();

	app.addRoutes(
		[
			{
				handler: ActionURLScreen,
				path: function(url) {
					var uri = new Uri(url);

					var loginRedirect = new Uri(Liferay.SPA.loginRedirect);

					var hostname = loginRedirect.getHostname() || window.location.hostname;

					if (!app.isLinkSameOrigin_(hostname)) {
						return false;
					}

					return uri.getParameterValue('p_p_lifecycle') === '1';
				}
			},
			{
				handler: RenderURLScreen,
				path: function(url) {
					if ((url + '/').indexOf(themeDisplay.getPathMain() + '/') === 0) {
						return false;
					}

					var excluded = Liferay.SPA.excludedPaths.some(
						(excludedPath) => url.indexOf(excludedPath) === 0
					);

					if (excluded) {
						return false;
					}

					var uri = new Uri(url);

					var lifecycle = uri.getParameterValue('p_p_lifecycle');

					return lifecycle === '0' || !lifecycle;
				}
			}
		]
	);

	Liferay.Util.submitForm = function(form) {
		async.nextTick(
			() => {
				let formElement = form.getDOM();
				let url = formElement.action;
				let formSelector = 'form' + Liferay.SPA.navigationExceptionSelectors;

				if (match(formElement, formSelector) && app.canNavigate(url) && (formElement.method !== 'get') && !app.isInPortletBlacklist(formElement)) {
					Liferay.Util._submitLocked = false;

					globals.capturedFormElement = formElement;

					const buttonSelector = 'button:not([type]),button[type=submit],input[type=submit]';

					if (match(globals.document.activeElement, buttonSelector)) {
						globals.capturedFormButtonElement = globals.document.activeElement;
					}
					else {
						globals.capturedFormButtonElement = form.one(buttonSelector);
					}

					app.navigate(utils.getUrlPath(url));
				}
				else {
					formElement.submit();
				}
			}
		);
	};

	Liferay.SPA.app = app;

	Liferay.fire('SPAReady');

	return app;
};

export default {
	init: function(callback) {
		if (globals.document.readyState == 'loading') {
			globals.document.addEventListener(
				'DOMContentLoaded',
				() => {
					callback.call(this, initSPA());
				}
			);
		}
		else {
			callback.call(this, initSPA());
		}
	}
};