sap.ui.define([
	], function () {
		"use strict";

		return {
			/**
			 * Rounds the currency value to 2 digits
			 *
			 * @public
			 * @param {string} sValue value to be formatted
			 * @returns {string} formatted currency value with 2 digits
			 */
			currencyValue : function (sValue) {
				if (sValue!== 0 && !sValue) {
					return "";
				}

				return parseFloat(sValue).toFixed(2);
			},
			
			/**
			 * Rounds the numbers to 2 digits
			 *
			 * @public
			 * @param {string} sValue value to be formatted
			 * @returns {string} formatted currency value with 0 digits
			 */
			numberFormatter : function (sValue) {
				if (sValue!== 0 && !sValue) {
					return "";
				}
				return parseFloat(sValue).toFixed(0);
			},
			
			/**
			 * Format the EDM Date to {@link sap.ui.model.type.Date}
			 *
			 * @public
			 * @param {integer} iDate EDM Date in millisecond
			 * @returns {@link sap.ui.model.type.Date} formatted date
			 */
			dateFormatter: function(iDate) {
				// treat undefined specially, as it is the case when the date is not applicable such as LastChangeDate is not applicable in case of Create event.
				if(iDate === undefined){
					return "NA";
				}
				if(!iDate){
					return;
				}
				var oType = new sap.ui.model.type.Date({style: "short"});
				return oType.formatValue(new Date(iDate), "string"); 
            }
		};

	}
);