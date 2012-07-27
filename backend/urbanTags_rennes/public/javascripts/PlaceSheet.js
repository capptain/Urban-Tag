    // "use strict";

/**
* This class represent the place sheet which displays details about a selected place. (description, event list)
*
* @class PlaceSheet
* @constructor
* @param {Object} data optional data given to the constructor to parametrize the place sheet
*/
function PlaceSheet(data)
{
    this.place = null;
    this.infoList = null;
    this.placeSummary = null;
    this.templateView = null;
    this.templateHtml = null;
    this.infoList = null;
    this.placeSummary = null;
    this.eventSelectedRegistration = null;
    this.eventUnSelectedRegistration = null;
    this.container = $("body");

    if(typeof data != "undefined"){
        if(typeof data.place != "undefined"){
            this.place = data.place;
        }
        if(typeof data.placeManager != "undefined"){
            this.placeManager = data.placeManager;
            this.placeSelectedRegistration = this.placeManager.register("placeSelected", this.onPlaceSelected.bind(this));
            this.placeUnselectedRegistration = this.placeManager.register("placeUnselected", this.onPlaceUnselected.bind(this));
        }
        if(typeof data.container != "undefined"){
            this.container = data.container;
        }
    }

    this.initView();
}

/**
* Initializes the view
*
* @method initView
* @param {Function} callback Optionnal function which will be executed at the end of the current function.
*/
PlaceSheet.prototype.initView = function(callback)
{
    if(this.templateView === null)
    {
        $.get(jsRoutes.place.sheet.template(),function(data){
            this.templateView = data;
            this.initView();
        }.bind(this)).error(function(data){
            // TODO: handle error
        }.bind(this));
        return;
    }
    if(this.templateHtml === null)
    {
        this.templateHtml = $(this.templateView);
    }
    if(this.infoList === null)
    {
        this.infoList = new InfoList($(".place-sheet-info-list-container", this.templateHtml), this.place);
    }
    if(this.placeSummary === null)
    {
        this.placeSummary = new PlaceSummary({'placeManager': this.placeManager, 'container': $(".place-sheet-summary-container", this.templateHtml), 'place': this.place});
    }
    if(this.eventSelectedRegistration === null)
    {
        this.eventSelectedRegistration = this.infoList.register("infoSelected", this.onEventSelected.bind(this));
    }
    if(this.eventUnSelectedRegistration === null)
    {
        this.eventUnSelectedRegistration = this.infoList.register("infoUnselected", this.onEventUnselected.bind(this));
    }
    if(typeof callback != "undefined")
    {
        callback();
    }
};

/**
* Display the view of the place sheet
*
* @method show
*/
PlaceSheet.prototype.show = function()
{
    if(this.templateView === null)
    {
        this.initView(function(){this.show();}.bind(this));
        return;
    }

    this.container.html(this.templateHtml);
    this.fillTemplate();
};

/**
* Hide the view of the object
*
* @method hide
*/
PlaceSheet.prototype.hide = function()
{
    this.templateHtml.remove();
    this.infoList.hide();
    this.placeSummary.hide();
};

/**
* Fill the template html view with object's data
*
* @method fillTemplate
*/
PlaceSheet.prototype.fillTemplate = function()
{
    if(this.place !== null){
        $(".place-title", this.templateHtml).html(this.place.name);
        $(".place-owner", this.templateHtml).html("créé par " + this.place.owner.username);
        var tagsHtml = "";
        for(var i = 0; i < this.place.tags.length; i++)
        {
            var tag = this.place.tags[i];
            tagsHtml += "<span class='label' style='background:#"+tag.color+";'>"+tag.name+"</span> ";
        }
        $("p.tag-list", this.templateHtml).html(tagsHtml);

        $(".description-tab-button", this.templateHtml).bind('click', this.displayDescription.bind(this));
        $(".events-tab-button", this.templateHtml).bind('click', this.displayEvents.bind(this));

        if($(".description-tab-button", this.templateHtml).hasClass("active"))
        {
            this.placeSummary.show();
            this.infoList.hide();
        }
        else if($(".events-tab-button", this.templateHtml).hasClass("active"))
        {
            this.infoList.show();
            this.placeSummary.hide();
        }
    }
};

/**
* Handles a placeSelected event. Display details about this place in the place sheet's view.
*
* @method onPlaceSelected
* @param {Object} place Selected place
*/
PlaceSheet.prototype.onPlaceSelected = function(place)
{
    this.onEventUnselected();
    this.place = place;
    this.placeSummary.onPlaceSelected(place);
    this.infoList.setPlace(place);
    this.show();
};

/**
* Handles a placeUnselected event. Hide details about the previous selected place
*
* @method onPlaceUnselected
* @param {Object} place Unselected place.
*/
PlaceSheet.prototype.onPlaceUnselected = function(place)
{
    this.place = null;
    this.placeSummary.onPlaceUnselected(place);
    this.placeSummary.hide();
    this.infoList.clear();
    this.hide();
};

/**
* Handles an infoSelected event. Display content of the selected event.
*
* @method onEventSelected
* @param {Object} event Selected event
*/
PlaceSheet.prototype.onEventSelected = function(event)
{
    $(".place-sheet-info-content", this.templateHtml).fadeOut(100,
    function(){
        var container = $(".place-sheet-info-content-container", this.templateHtml);
        showLoader(container);
        container.show();
        $.get(jsRoutes.info.webContent({'id': event.id}), function(data)
        {
            hideLoader(container);
            $(".place-sheet-info-content", this.templateHtml).html(data);
            $(".place-sheet-info-content", this.templateHtml).fadeIn(100);
        }.bind(this)).error(function(data){
         // TODO: handle error
        });
    });
};

/**
* Handles an infoUnselected event. Hide the event content's container
*
* @method onEventUnselected
* @param {Object} event Unselected event.
*/
PlaceSheet.prototype.onEventUnselected = function(event)
{
    $(".place-sheet-info-content-container", this.templateHtml).fadeOut(100);
};

/**
* Display the place description content's container with the description of the selected place
*
* @method displayDescription
*/
PlaceSheet.prototype.displayDescription = function()
{
    $(".description-tab-button", this.templateHtml).addClass("active");
    $(".events-tab-button", this.templateHtml).removeClass("active");
    $(".place-sheet-info-content-container", this.templateHtml).hide();
    this.placeSummary.show();
    this.infoList.hide();
};

/**
* Display the place event list's container of the selected place
*
* @method displayEvents
*/
PlaceSheet.prototype.displayEvents = function()
{
    $(".description-tab-button", this.templateHtml).removeClass("active");
    $(".events-tab-button", this.templateHtml).addClass("active");
    $(".place-sheet-info-content-container", this.templateHtml).hide();
    this.infoList.show();
    this.placeSummary.hide();
};