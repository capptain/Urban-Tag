(function(){
    "use strict";
})();

function PlaceSheet(data)
{
    this.place = null;
    this.infoList = null;
    this.placeSummary = null;
    this.templateView = null;
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

    $.get(jsRoutes.place.sheet.template(), function(data){
        this.templateView = data;
        this.templateHtml = $(this.templateView);

        this.infoList = new InfoList($(".place-sheet-info-list-container", this.templateHtml));

        this.eventSelectedRegistration = this.infoList.register("infoSelected", this.onEventSelected.bind(this));
        this.eventUnSelectedRegistration = this.infoList.register("infoUnselected", this.onEventUnselected.bind(this));

        this.placeSummary = new PlaceSummary({'placeManager': this.placeManager, 'container': $(".place-sheet-summary-container", this.templateHtml)});
    }.bind(this)).error(function(data){
        // TODO: handle error
    }.bind(this));
}

PlaceSheet.prototype.show = function()
{
    if(this.templateView === null)
    {
        $.get(jsRoutes.place.sheet(), function(data){
            this.templateView = data;
            this.show();
        }.bind(this)).error(function(data){
            // TODO: handle error
        }.bind(this));
        return;
    }

    if(this.templateHtml === null){
        this.templateHtml = $(this.templateView);
    }

    this.container.html(this.templateHtml);
    this.fillTemplate();
};

PlaceSheet.prototype.hide = function()
{
    this.templateHtml.remove();
    this.infoList.hide();
    this.placeSummary.hide();
};

PlaceSheet.prototype.fillTemplate = function()
{
    if(this.place !== null){
        if(this.infoList === null)
        {
            this.infoList = new InfoList($(".place-sheet-info-list-container", this.templateHtml), this.place);
        }

        if(this.placeSummary === null)
        {
            this.placeSummary = new PlaceSummary({'placeManager': this.placeManager, 'container': $(".place-sheet-summary-container", this.templateHtml), 'place': this.place});
        }

        // this.infoList.show();
        // this.placeSummary.show();
    }
};

PlaceSheet.prototype.onPlaceSelected = function(place)
{
    this.place = place;
    this.show();
    this.placeSummary.onPlaceSelected(place);
    this.infoList.setPlace(place);
};

PlaceSheet.prototype.onPlaceUnselected = function(place)
{
    this.place = null;
    this.placeSummary.onPlaceUnselected(place);
    this.infoList.clear();
    this.hide();
};

PlaceSheet.prototype.onEventSelected = function(event)
{
    $(".place-sheet-info-content-container", this.templateHtml).html(event.content);
};

PlaceSheet.prototype.onEventUnselected = function(event)
{
    $(".place-sheet-info-content-container", this.templateHtml).html("");
};