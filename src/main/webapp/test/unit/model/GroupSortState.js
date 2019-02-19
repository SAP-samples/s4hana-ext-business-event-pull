sap.ui.define([
		"behsample/model/GroupSortState",
		"sap/ui/model/json/JSONModel"
	], function (GroupSortState, JSONModel) {
	"use strict";

	QUnit.module("GroupSortState - grouping and sorting", {
		beforeEach: function () {
			this.oModel = new JSONModel({});
			// System under test
			this.oGroupSortState = new GroupSortState(this.oModel, function() {});
		}
	});

	QUnit.test("Should always return a sorter when sorting", function (assert) {
		// Act + Assert
		assert.strictEqual(this.oGroupSortState.sort("BusEventPriority").length, 1, "The sorting by BusEventPriority returned a sorter");
		assert.strictEqual(this.oGroupSortState.sort("SAPObjectTaskTypeName").length, 1, "The sorting by SAPObjectTaskTypeName returned a sorter");
	});

	QUnit.test("Should return a grouper when grouping", function (assert) {
		// Act + Assert
		assert.strictEqual(this.oGroupSortState.group("BusEventPriority").length, 1, "The group by BusEventPriority returned a sorter");
		assert.strictEqual(this.oGroupSortState.group("None").length, 0, "The sorting by None returned no sorter");
	});


	QUnit.test("Should set the sorting to BusEventPriority if the user groupes by BusEventPriority", function (assert) {
		// Act + Assert
		this.oGroupSortState.group("BusEventPriority");
		assert.strictEqual(this.oModel.getProperty("/sortBy"), "BusEventPriority", "The sorting is the same as the grouping");
	});

	QUnit.test("Should set the grouping to None if the user sorts by SAPObjectTaskTypeName and there was a grouping before", function (assert) {
		// Arrange
		this.oModel.setProperty("/groupBy", "BusEventPriority");

		this.oGroupSortState.sort("SAPObjectTaskTypeName");

		// Assert
		assert.strictEqual(this.oModel.getProperty("/groupBy"), "None", "The grouping got reset");
	});
});