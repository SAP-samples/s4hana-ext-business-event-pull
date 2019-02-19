jQuery.sap.require("sap.ui.qunit.qunit-css");
jQuery.sap.require("sap.ui.thirdparty.qunit");
jQuery.sap.require("sap.ui.qunit.qunit-junit");
QUnit.config.autostart = false;

// We cannot provide stable mock data out of the template.
// If you introduce mock data, by adding .json files in your webapp/localService/mockdata folder you have to provide the following minimum data:
// * At least 3 C_Behqueuedata in the list

sap.ui.require([
	"sap/ui/test/Opa5",
	"behsample/test/integration/pages/Common",
	"sap/ui/test/opaQunit",
	"behsample/test/integration/pages/App",
	"behsample/test/integration/pages/Browser",
	"behsample/test/integration/pages/Master",
	"behsample/test/integration/pages/Detail",
	"behsample/test/integration/pages/NotFound"
], function (Opa5, Common) {
	"use strict";
	Opa5.extendConfig({
		arrangements: new Common(),
		viewNamespace: "behsample.view."
	});

	sap.ui.require([
		"behsample/test/integration/MasterJourney",
		"behsample/test/integration/NavigationJourney",
		"behsample/test/integration/NotFoundJourney",
		"behsample/test/integration/BusyJourney"
	], function () {
		QUnit.start();
	});
});