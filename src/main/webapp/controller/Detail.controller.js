/*global location */
sap.ui.define([
		"behsample/controller/BaseController",
		"sap/ui/model/json/JSONModel",
		"behsample/model/formatter",
		"sap/m/MessageToast"
	], function (BaseController, JSONModel, formatter, MessageToast) {
		"use strict";

		return BaseController.extend("behsample.controller.Detail", {

			formatter: formatter,

			/* =========================================================== */
			/* lifecycle methods                                           */
			/* =========================================================== */

			onInit : function () {
				// Model used to manipulate control states. The chosen values make sure,
				// detail page is busy indication immediately so there is no break in
				// between the busy indication for loading the view's meta data
				var oViewModel = new JSONModel({
					busy : false,
					delay : 0
				});

				this.getRouter().getRoute("object").attachPatternMatched(this._onObjectMatched, this);

				this.setModel(oViewModel, "detailView");

				this.getOwnerComponent().getModel().pSequentialImportCompleted.then(this._onRequestCompleted.bind(this));
			},


			/* =========================================================== */
			/* begin: internal methods                                     */
			/* =========================================================== */
			
			/**
			 * Binds the view to the object path.
			 * @function
			 * @param {sap.ui.base.Event} oEvent pattern match event in route 'object'
			 * @private
			 */
			_onObjectMatched : function (oEvent) { 
				var oViewModel = this.getModel("detailView");
				oViewModel.setProperty("/busy", true); 

				var sObjectId =  oEvent.getParameter("arguments").objectId;

				this.getModel().pSequentialImportCompleted.then( // Attach a "done" handler for the asyncEvent
					function() {
						if(this.getModel().errorOcurred){
							oViewModel.setProperty("/busy", true);
							return;
						}
						
						//we get the binding path of the selected item of the list.
						//as "multi-selection" is not enabled, there can be only one selected item, so the returned array will have only one element.
						var sBindingPath = this.getOwnerComponent().oListSelector._oList.getSelectedContextPaths()[0];
						
						//if no item is clicked, then page is reloaded. We need to find the item using Business Event ID.
						if(!sBindingPath){
							//find the item from id, find its index	
							var oList = this.getOwnerComponent().oListSelector._oList;
							var oListItems = oList.getItems();
							var iObjectIdx = oListItems.findIndex( function(item){ 
								return item.getBindingContext().getObject().BusinessEvent.BusinessEvent === sObjectId;});
							
							//click the item
							var oSelectedItem = oListItems[iObjectIdx];
							// if a user changes the url manually, or she refreshes the app after a time and the event is not available anymore
							//Then the index could be wrong and we then do not get a item to show. However, the router will still match the path.
							if(!oSelectedItem){
								this.getRouter().getTargets().display("notFound");
								return;
							}
							// to re-select the list item
							oList.setSelectedItem(oSelectedItem, true);
							
							//change the sBindingPath accordingly							
							sBindingPath = oSelectedItem.getBindingContextPath();
						}
												
						this.getView().bindElement({
							path: sBindingPath, 
							events: {
								change: function(){
									oViewModel.setProperty("/busy", false);
								}.bind(this)
							}
						});
					  }.bind(this)
				  );
			},
			
			_onRequestCompleted : function () {
				// Store original busy indicator delay for the detail view
				var iOriginalViewBusyDelay = this.getView().getBusyIndicatorDelay(),
					oViewModel = this.getModel("detailView");

				// Make sure busy indicator is displayed immediately when
				// detail view is displayed for the first time
				oViewModel.setProperty("/delay", 0);

				// Binding the view will set it to not busy - so the view is always busy if it is not bound
				oViewModel.setProperty("/busy", true);
				// Restore original busy indicator delay for the detail view
				oViewModel.setProperty("/delay", iOriginalViewBusyDelay);
			}

		});

	}
);