// "use strict";

/**
* The place summary object displays the description of a place.
*
* @class PlaceSummary
* @constructor
* @param {Object} data Optional data to parametrize the PlaceSummary.
*/
function PlaceSummary(data)
{
    this.container = $("body");
    this.place     = null;

    if(typeof data != "undefined")
    {
        if(typeof data.container != "undefined"){
            this.container = data.container;
        }

        if(typeof data.place != "undefined"){
            this.place = data.place;
        }
    }
    
    this.infoWizard             = new InfoWizard();
    this.infoAddedRegistration  = this.infoWizard.register("infoAdded", this.onInfoAdded.bind(this));
    this.infoEditedRegistration = this.infoWizard.register("infoEdited", this.onInfoEdited.bind(this));
    
    this.templateView = null;
    this.templateHtml = null;
    
    this.description = null;
}

/**
* Show the place summary view
*
* @method show
*/
PlaceSummary.prototype.show = function()
{
   if(this.templateView === null)
   {
       $.get(jsRoutes.place.summary.getTemplate(), function(data)
       {
           this.templateView = data;
           this.show();
       }.bind(this)).error(function(data)
       {
           // TODO: handle error
       }.bind(this));
       return;
   }
   
   this.templateHtml = $(this.templateView);
   this.container.html(this.templateHtml);
   
   $(".place-summary-button-add-description-info", this.templateHtml).bind('click', this.onAddButtonClicked.bind(this));
   $(".place-summary-button-edit-description-info", this.templateHtml).bind('click', this.onEditButtonClicked.bind(this));
   $(".place-summary-button-delete-description-info", this.templateHtml).bind('click', this.onDeleteButtonClicked.bind(this));
   
   this.loadPlaceSummary();
};

/**
* Hide the place summary view
*
* @method hide
*/
PlaceSummary.prototype.hide = function()
{
    if(this.templateHtml !== null)
        this.templateHtml.hide();
};

/**
* Display the message indicating there is no description for the current place
*
* @method displayEmptyDescriptionMessage
*/
PlaceSummary.prototype.displayEmptyDescriptionMessage = function()
{
    if(this.emptyMessage === null)
    {
        showLoader(this.templateHtml);
        $.get(jsRoutes.place.summary.emptyView(), function(data)
        {
            this.emptyMessage = $(data);
            this.displayEmptyDescriptionMessage();
        }.bind(this));
        return;
    }
    
    hideLoader(this.templateHtml);
    $(".place-summary-description", this.templateHtml).html(this.emptyMessage);
};

/**
* Load the place description from the server and display it.
*
* @method loadPlaceSummary
*/
PlaceSummary.prototype.loadPlaceSummary = function()
{
    if(this.place !== null)
    {
        showLoader(this.templateHtml);

        /*
         * Load place description
         */
        $(".place-summary-button-edit-description-info", this.templateHtml).hide();
        $(".place-summary-button-add-description-info", this.templateHtml).hide();
        $(".place-summary-button-delete-description-info", this.templateHtml).hide();
        $.get(jsRoutes.getPlaceDescription({'placeId': this.place.id}), function(data)
        {
            hideLoader(this.templateHtml);
            if(data !== "" && typeof data.content != "undefined")
            {
                this.description = data;
                if(typeof myName != "undefined"){
                    if(this.place.owner.username == myName)
                    {
                        $(".place-summary-button-edit-description-info", this.templateHtml).show();
                        $(".place-summary-button-delete-description-info", this.templateHtml).show();
                    }
                }
                $.post(jsRoutes.info.parseContent({'content': encodeURIComponent(data.content)}), function(data)
                {
                    $(".place-summary-description", this.templateHtml).html(data);
                }.bind(this)).error(function(data){
                    
                });
            }
            else
            {
                if(this.place.owner.username == myName)
                {
                    $(".place-summary-button-add-description-info", this.templateHtml).show();
                }
                this.displayEmptyDescriptionMessage();
            }
        }.bind(this)).error(function(data){
            hideLoader(this.templateHtml);
            
            if(typeof data.status != "undefined" && data.status === 0)
            {
                displayAlert("La connexion avec le serveur a été rompue, impossible de récupérer la description");
            }
            else
            {
                displayAlert("Oups, erreur inconnue.");
            }
        }.bind(this));
    }
    else
    {
        this.hide();
    }
};

/**
* Handles the click event on the "add description" button. Displays the description adding wizard
*
* @method onAddButtonClicked
*/
PlaceSummary.prototype.onAddButtonClicked = function()
{
    this.infoWizard.startCreatingDescription(this.place);
};

/**
* Handles the click event on the "edit description" button. Display the description editing wizard.
*
* @method onEditButtonClicked
*/
PlaceSummary.prototype.onEditButtonClicked = function()
{
    this.infoWizard.startEditingDescription(this.description);
};

/**
* Handles the click event on the "delete description" button. Delete the description.
*
* @method onDeleteButtonClicked
*/
PlaceSummary.prototype.onDeleteButtonClicked = function()
{
    $.get(jsRoutes.info.remove({'id': this.description.id}), function(data)
    {
        this.onPlaceSelected(this.place);
    }.bind(this)).error(function(data){
        
    }.bind(this));
};

/**
* Executed when a place is selected. Just set the place variable.
*
* @method onPlaceSelected
* @param {Object} place Selected place.
*/
PlaceSummary.prototype.onPlaceSelected = function(place)
{
    this.place = place;
};

/**
* Executed when a place is unselected. Just set the place variable to null.
*
* @method onPlaceUnselected
* @param {Object} place Unselected place.
*/
PlaceSummary.prototype.onPlaceUnselected = function(place)
{
    this.place = null;
};

/**
* Executed when a description is added. Show the place summary with the new description
*
* @method onInfoAdded
* @param {Object} info Added description
*/
PlaceSummary.prototype.onInfoAdded = function(info)
{
    this.show();
};

/**
* Executed when a description is edited. Show the place summary with the new description
*
* @method onInfoEdited
* @param {Object} info Edited description
*/
PlaceSummary.prototype.onInfoEdited = function(info)
{
    this.show();
};