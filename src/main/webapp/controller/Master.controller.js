/*global history */
sap.ui.define([
		"behsample/controller/BaseController",
		"sap/ui/model/json/JSONModel",
		"sap/ui/model/Filter",
		"sap/ui/model/FilterOperator",
		"sap/m/GroupHeaderListItem",
		"sap/ui/Device",
		"behsample/model/formatter"
	], function (BaseController, JSONModel, Filter, FilterOperator, GroupHeaderListItem, Device, formatter) {
		"use strict";

		return BaseController.extend("behsample.controller.Master", {

			formatter: formatter,

			/* =========================================================== */
			/* lifecycle methods                                           */
			/* =========================================================== */

			/**
			 * Called when the master list controller is instantiated. It sets up the event handling for the master/detail communication and other lifecycle tasks.
			 * @public
			 */
			onInit : function () {
				// Control state model
				var oList = this.byId("list"),
					oViewModel = this._createViewModel(),
					// Put down master list's original value for busy indicator delay,
					// so it can be restored later on. Busy handling on the master list is
					// taken care of by the master list itself.
					iOriginalBusyDelay = oList.getBusyIndicatorDelay();
				
				this._oList = oList;
				// keeps the filter and search state
				this._oListFilterState = {
					aFilter : [],
					aSearch : []
				};
				
				this.setModel(oViewModel, "masterView");
				// Make sure, busy indication is showing immediately so there is no
				// break after the busy indication for loading the view's meta data is
				// ended (see promise 'oWhenMetadataIsLoaded' in AppController)
				oList.attachEventOnce("updateFinished", function(){
					// Restore original busy indicator delay for the list
					oViewModel.setProperty("/delay", iOriginalBusyDelay);
				});

				this.getView().addEventDelegate({
					onBeforeFirstShow: function () {
						this.getOwnerComponent().oListSelector.setBoundMasterList(oList);
					}.bind(this)
				});

				this.getRouter().getRoute("master").attachPatternMatched(this._onMasterMatched, this);
				this.getRouter().attachBypassed(this.onBypassed, this);
			},

			/* =========================================================== */
			/* event handlers                                              */
			/* =========================================================== */

			/**
			 * After list data is available, this handler method updates the
			 * master list counter and hides the pull to refresh control, if
			 * necessary.
			 * @param {sap.ui.base.Event} oEvent the update finished event
			 * @public
			 */
			onUpdateFinished : function (oEvent) {
				// update the master list object counter after new data is loaded
				this._updateListItemCount(oEvent.getParameter("total"));
			},

			/**
			 * Event handler for the master search field. Applies current
			 * filter value and triggers a new search. If the search field's
			 * 'refresh' button has been pressed, no new search is triggered
			 * and the list binding is refresh instead.
			 * @param {sap.ui.base.Event} oEvent the search event
			 * @public
			 */
			onSearch : function (oEvent) {
				if (oEvent.getParameters().refreshButtonPressed) {
					// Search field's 'refresh' button has been pressed.
					// This is visible if you select any master list item.
					// In this case no new search is triggered
					return;
				}

				var sQuery = oEvent.getParameter("query");

				if (sQuery) {
					this._oListFilterState.aSearch = [new sap.ui.model.Filter( // a filter which applies OR condition
						[new sap.ui.model.Filter("BusinessEvent/SAPObjectTaskTypeName", FilterOperator.Contains, sQuery),
						new sap.ui.model.Filter("BusinessEvent/SAPBusinessObjectKey1", FilterOperator.Contains, sQuery),
						new sap.ui.model.Filter("BusinessObject/PurchaseOrderByCustomer", FilterOperator.Contains, sQuery),
						new sap.ui.model.Filter("BusinessObject/SoldToParty", FilterOperator.Contains, sQuery)],
						false)];
				} else {
					this._oListFilterState.aSearch = [];
				}
				this._applyFilterSearch();

			},

			/**
			 * Event handler called when ViewSettingsDialog has been confirmed, i.e.
			 * has been closed with 'OK'. In the case, the currently chosen filters
			 * are applied to the master list, which can also mean that the currently
			 * applied filters are removed from the master list, in case the filter
			 * settings are removed in the ViewSettingsDialog.
			 * @param {sap.ui.base.Event} oEvent the confirm event
			 * @public
			 */
			onConfirmViewSettingsDialog : function (oEvent) {
				var aFilterItems = oEvent.getParameters().filterItems,
					aFilters = [],
					aCaptions = [];

				// update filter state:
				// combine the filter array and the filter string
				aFilterItems.forEach(function (oItem) {
					switch (oItem.getKey()) {
						case "Filter1" :
							aFilters.push(new Filter("BusEvent/BusEventPriority", FilterOperator.LE, 100));
							break;
						case "Filter2" :
							aFilters.push(new Filter("BusEvent/BusEventPriority", FilterOperator.GT, 100));
							break;
						default :
							break;
					}
					aCaptions.push(oItem.getText());
				});

				this._oListFilterState.aFilter = aFilters;
				this._updateFilterBar(aCaptions.join(", "));
				this._applyFilterSearch();
			},

			/**
			 * Event handler for the list selection event
			 * @param {sap.ui.base.Event} oEvent the list selectionChange event
			 * @public
			 */
			onSelectionChange : function (oEvent) {
				// get the list item, either from the listItem parameter or from the event's source itself (will depend on the device-dependent mode).
				this._showDetail(oEvent.getParameter("listItem") || oEvent.getSource());
			},

			/**
			 * Event handler for the bypassed event, which is fired when no routing pattern matched.
			 * If there was an object selected in the master list, that selection is removed.
			 * @public
			 */
			onBypassed : function () {
				this._oList.removeSelections(true);
			},

			/**
			 * Used to create GroupHeaders with non-capitalized caption.
			 * These headers are inserted into the master list to
			 * group the master list's items.
			 * @param {Object} oGroup group whose text is to be displayed
			 * @public
			 * @returns {sap.m.GroupHeaderListItem} group header with non-capitalized caption.
			 */
			createGroupHeader : function (oGroup) {
				return new GroupHeaderListItem({
					title : oGroup.text,
					upperCase : false
				});
			},

			/**
			 * Event handler for navigating back.
			 * We navigate back in the browser history
			 * @public
			 */
			onNavBack : function() {
				history.go(-1);
			},

			/* =========================================================== */
			/* begin: internal methods                                     */
			/* =========================================================== */


			_createViewModel : function() {
				return new JSONModel({
					isFilterBarVisible: false,
					filterBarLabel: "",
					delay: 0,
					title: this.getResourceBundle().getText("masterTitleCount", [0]),
					noDataText: this.getResourceBundle().getText("masterListNoDataText"),
					sortBy: "SAPObjectTaskTypeName",
					groupBy: "None"
				});
			},

			/**
			 * If the master route was hit (empty hash) we have to set
			 * the hash to to the first item in the list as soon as the
			 * listLoading is done and the first item in the list is known
			 * @private
			 */
			_onMasterMatched :  function() {
				
				this.getModel().pSequentialImportCompleted.then(function() {
					if(this.getModel().errorOcurred){
						this.getModel("masterView").setProperty("/noDataText", this.getResourceBundle().getText("masterListTechnicalError"));
						return;
					}
					
					// get the first item on the list
					var oList = this.byId("list");
					var oItems = oList.getItems();
					if (!jQuery.isArray(oItems) || oItems.length === 0) {
						// if items are not available, then navigate to the not found
						this.getRouter().getTargets().display("detailNoObjectsAvailable");
						return;
					};
					var firstItem = oItems[0];
					var sObjectId = firstItem.getBindingContext().getProperty("BusinessEvent/BusinessEvent");
					// trigger "click" event
					oList.setSelectedItem(firstItem, true); 
					// and navigate to that item
					this.getRouter().navTo("object", {objectId : sObjectId}, true);
				}.bind(this));				
			},

			/**
			 * Shows the selected item on the detail page
			 * On phones a additional history entry is created
			 * @param {sap.m.ObjectListItem} oItem selected Item
			 * @private
			 */
			_showDetail : function (oItem) {
				var bReplace = !Device.system.phone;
				
				this.getRouter().navTo("object", {
					objectId : oItem.getBindingContext().getProperty("BusinessEvent/BusinessEvent")
				}, bReplace);
			},

			/**
			 * Sets the item count on the master list header
			 * @param {integer} iTotalItems the total number of items in the list
			 * @private
			 */
			_updateListItemCount : function (iTotalItems) {
				var sTitle;
				// only update the counter if the length is final
				if (this._oList.getBinding("items").isLengthFinal()) {
					sTitle = this.getResourceBundle().getText("masterTitleCount", [iTotalItems]);
					this.getModel("masterView").setProperty("/title", sTitle);
				}
			},

			/**
			 * Internal helper method to apply both filter and search state together on the list binding
			 * @private
			 */
			_applyFilterSearch : function () {
				var aFilters = this._oListFilterState.aSearch.concat(this._oListFilterState.aFilter),
					oViewModel = this.getModel("masterView");
				this._oList.getBinding("items").filter(aFilters, "Application");
				// changes the noDataText of the list in case there are no filter results
				if (aFilters.length !== 0) {
					oViewModel.setProperty("/noDataText", this.getResourceBundle().getText("masterListNoDataWithFilterOrSearchText"));
				} else if (this._oListFilterState.aSearch.length > 0) {
					// only reset the no data text to default when no new search was triggered
					oViewModel.setProperty("/noDataText", this.getResourceBundle().getText("masterListNoDataText"));
				}
			},

			/**
			 * Internal helper method to apply both group and sort state together on the list binding
			 * @param {sap.ui.model.Sorter[]} aSorters an array of sorters
			 * @private
			 */
			_applyGroupSort : function (aSorters) {
				this._oList.getBinding("items").sort(aSorters);
			},

			/**
			 * Internal helper method that sets the filter bar visibility property and the label's caption to be shown
			 * @param {string} sFilterBarText the selected filter value
			 * @private
			 */
			_updateFilterBar : function (sFilterBarText) {
				var oViewModel = this.getModel("masterView");
				oViewModel.setProperty("/isFilterBarVisible", (this._oListFilterState.aFilter.length > 0));
				oViewModel.setProperty("/filterBarLabel", this.getResourceBundle().getText("masterFilterBarText", [sFilterBarText]));
			}

		});

	}
);