sap.ui.define([
		"sap/ui/base/Object",
		"sap/m/MessageBox"
	], function (UI5Object, MessageBox) {
		"use strict";

		return UI5Object.extend("behsample.controller.ErrorHandler", {

			/**
			 * Handles application errors by automatically attaching to the model events and displaying errors when needed.
			 * @class
			 * @param {sap.ui.core.UIComponent} oComponent reference to the app's component
			 * @public
			 * @alias behsample.controller.ErrorHandler
			 */
			constructor : function (oComponent) {
				this._oResourceBundle = oComponent.getModel("i18n").getResourceBundle();
				this._oComponent = oComponent;
				this._oModel = oComponent.getModel();
				this._bMessageOpen = false;
				this._sErrorText = this._oResourceBundle.getText("errorText");
				this._limitationShown = false;

				this._oModel.attachRequestCompleted(function (oEvent) {
					var oParams = oEvent.getParameters();
					var errorOcurred = !oParams.success;
					this._oModel.errorOcurred = errorOcurred;
					
					if(errorOcurred)
						this._showServiceError(oParams.errorobject);
					else if(this._oModel.getData().limited)
						this._showLimitationWarning();
						
					
				}, this);
			},

			/**
			 * Shows a {@link sap.m.MessageBox} when a service call has failed.
			 * Only the first error message will be display.
			 * @param {object} oDetails a technical error object to be displayed on request
			 * @private
			 */
			_showServiceError : function (oDetails) {
				if (this._bMessageOpen) {
					return;
				}
				var sDetails = this._prepareErrorDetailsToShow(oDetails);
				console.log(sDetails);
				this._bMessageOpen = true;
				MessageBox.error(
					this._sErrorText,
					{
						id : "serviceErrorMessageBox",
						details : sDetails,
						styleClass : this._oComponent.getContentDensityClass(),
						actions : [MessageBox.Action.CLOSE],
						onClose : function () {
							this._bMessageOpen = false;
						}.bind(this)
					}
				);
			},
			
			
			/**
			 * Prepares an object to show in the {@link sap.m.MessageBox}.
			 * @param {object} oDetails a technical error object received from backend
			 * @private
			 * @returns {string} formatted text to show in {@link sap.m.MessageBox} as details.
			 */
			_prepareErrorDetailsToShow : function (oDetails) {
				// parsing the received JSON formatted responseText
				var oErrorResponseText = JSON.parse(oDetails.responseText);
				
				//creating the string in HTML form to show
				
				var sDetailsInHTML = 
					`<p>Backend responded with the following information:<br>
					HTML Status Code: ${oDetails.statusCode}<br>
					Message: ${oDetails.message}<br>
					Error Code: ${oErrorResponseText.error.code}<br>
					Error Message: "${oErrorResponseText.error.message.value}" </p>
					
					<p><strong>This can happen if:</strong></p>\n
					<ul>
					<li>Backend system is down</li>
					<li>Wrong S/4 destination configuration</li>
					<li>Missing/wrong communication arrangement</li>
					<li>Technical user is locked due to several log on trial with wrong credentials<br>
					You can simply check this by using <em>Display Technical Users</em> app in your S/4HANA.<br>
					If this is the case, you can simply unclock your user in the app as well.</li>
					<li>a backend component is not <em>available</em></li>
					<li>You are not connected to the internet</li>
					</ul>
					<p>Get more help <a href="https://help.sap.com/viewer/index" target="_top">here</a>.`;

				return sDetailsInHTML;
			},
			
			/**
			 * Method for showing warning in case of limitation problem.
			 * @public
			 */
			_showLimitationWarning : function () {
				if(this._limitationShown)
					return;
				this._limitationShown = true;
				MessageBox.warning(
						"Due to the limitation on the length of a query to S/4HANA OData service\n" +
						"We recommend to always limit the number of requested entities.\n\n" +
						"If you need to show more items, please consider implementing your own pagination logic.\n" +
						"Please check BehController class for more details.",
						{
							id : "limitationWarningMessageBox",
							styleClass : this._oComponent.getContentDensityClass(),
							actions : [MessageBox.Action.CLOSE]
						}
					);
			}

		});

	}
);