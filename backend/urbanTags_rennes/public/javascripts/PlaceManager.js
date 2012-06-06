"use strict";

function PlaceManager()
{
    CanFireEvents.call(this, ["placeAdded", "placeEdited", "placeSelected", "placeUnselected"]);
	this.places = new Array();
	this.selectedPlace = null;
};

extend(PlaceManager.prototype, CanFireEvents.prototype);

/**
 * Load places from the server
 */
PlaceManager.prototype.loadData = function()
{
    this.places = new Array();
    var obj = this;
    $.get(jsRoutes.getPlaceListAction(), function(data){
        jQuery.each(data, function(i, place)
        {
            obj.addPlace(this);
        });
    });
};

PlaceManager.prototype.getPlaceByName = function(name)
{
    var found = false;
    var cpt = 0;
    while(!found && cpt < this.places.length)
    {
        found = (this.places[cpt].name === name);
        if(!found)
        {
            cpt++;
        }
    }
    
    if(found)
    {
        return this.places[cpt];
    }
    else
    {
        return null;
    }
}

/**
 * Add a place to the place list and fire a placeAddedEvent
 */
PlaceManager.prototype.addPlace = function(place)
{
    this.places.push(place);
    this.fireEvent("placeAdded", place);
};


PlaceManager.prototype.selectPlaceByName = function(name)
{
    var place = this.getPlaceByName(name);
    this.selectPlace(place);
};

PlaceManager.prototype.selectPlace = function(place)
{
    var index = this.places.indexOf(place);
    if(index > -1)
    {
        this.selectPlaceByIndex(index);
    }
};

PlaceManager.prototype.selectPlaceByIndex = function(index)
{
    if(index >= 0 && index < this.places.length)
    {
        this.selectedPlace = this.places[index];
        this.fireEvent("placeSelected", this.selectedPlace);
    }
};

PlaceManager.prototype.unselectPlace = function()
{
    this.selectedPlace = null;
    this.fireEvent("placeUnselected");
};