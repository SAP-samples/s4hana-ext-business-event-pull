jQuery.sap.require("sap.ui.qunit.qunit-css");
jQuery.sap.require("sap.ui.thirdparty.qunit");
jQuery.sap.require("sap.ui.qunit.qunit-junit");
QUnit.config.autostart = false;

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
		"behsample/test/integration/NavigationJourneyPhone",
		"behsample/test/integration/NotFoundJourneyPhone",
		"behsample/test/integration/BusyJourneyPhone"
	], function () {
		QUnit.start();
	});
});