"use strict";

function PlaceManager()
{
    CanFireEvents.call(this, ["placeAdded", "placeEdited", "placeSelected", "placeUnselected", "placeDeleted"]);
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

PlaceManager.prototype.editPlace = function(place)
{
    // Find old place
    var id = place.id;
    var found = false;
    var i = 0;
    var oldPlace = null;
    while(!found  && i < this.places.length)
    {
        found = this.places[i].id == id;
        if(found)
        {
            oldPlace = this.places[i];
        }
        i++;
    }
    
    if(found)
    {
        for(var key in place)
        {
            oldPlace[key] = place[key];
        }
        
        this.fireEvent("placeEdited", oldPlace);
    }
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
        this.selectedPlace = place;
        this.fireEvent("placeSelected", place);
//        this.selectPlaceByIndex(index);
    }
};

PlaceManager.prototype.deletePlace = function(place)
{
	$.get(jsRoutes.place.remove({'id':place.id}), function(data)
	{
		console.log(data);
		var index = this.places.indexOf(place);
		var newPlaces = [];
		for(var i = 0; i < index; i++)
		{
			newPlaces.push(this.places[i]);
		}
		
		for(var i = index+1; i < this.places.length; i++)
		{
			newPlaces.push(this.places[i]);
		}
		
		this.places = newPlaces;
		if(this.selectedPlace == place)
		{
			this.unselectPlace();
		}
		
		this.fireEvent("placeDeleted", place);
	}.bind(this));
};

PlaceManager.prototype.unselectPlace = function()
{
    this.selectedPlace = null;
    this.fireEvent("placeUnselected");
};