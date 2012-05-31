"use strict";

function PlaceManager()
{
	this.places = new Array();
	this.selectedPlace = null;
	this.listeners = {
			"placeAdded": new Array(),
			"placeEdited": new Array(),
			"placeSelected": new Array()
	};
};

/**
 * Load places from the server
 */
PlaceManager.prototype.loadData = function()
{
    this.places = new Array();
    var obj = this;
    $.get(getPlaceListAction(), function(data){
        jQuery.each(data, function(i, place)
        {
            obj.addPlace(this);
        });
    });
};

/**
 * Add a place to the place list and fire a placeAddedEvent
 */
PlaceManager.prototype.addPlace = function(place)
{
    this.places.push(place);
    this.fireEvent("placeAdded", place);
};


PlaceManager.prototype.selectPlace = function(index)
{
    if(index >= 0 && index < this.places.length)
    {
        this.selectedPlace = this.places[index];
        this.fireEvent("placeSelected", this.selectedPlace);
    }
};

/**
 * Register an object to the placeAddedEvent
 */
PlaceManager.prototype.addPlaceAddedListener = function(action)
{
    this.listeners["placeAdded"].push(action);
    return new EventRegistration(this, 'placeAdded', action);
};

/**
 * Register an object to the placeEditedEvent
 */
PlaceManager.prototype.addPlaceEditedListener = function(action)
{
    this.listeners["placeEdited"].push(action);
    return new EventRegistration(this, 'placeEdited', action);
};

/**
 * Register an object to the placeSelectedEvent
 */
PlaceManager.prototype.addPlaceSelectedListener = function(action)
{
    this.listeners["placeSelected"].push(action);
    return new EventRegistration(this, 'placeSelected', action);
};

PlaceManager.prototype.removeHandler = function(eventName, handler)
{           
    var found = false;
    var handlers = this.listeners[eventName];
    var cptHandler = 0;
    while(!found && cptHandler < handlers.length)
    {
        found = handlers[cptHandler] == handler;
        
        if(found)
        {
            if(handlers.length > 1)
            {
                for(var i = handlers.length - 1; i > cptHandlers; i--)
                {
                    this.listeners[eventName][i-1] = handlers[i];
                }
            }
            else
            {
                this.listeners[eventName] = new Array();
            }
        }
    }
};

/**
 * Fire an event of the given name with the given param
 */
PlaceManager.prototype.fireEvent = function(eventName, param)
{
    for(var i = 0; i < this.listeners[eventName].length; i++)
    {
        var handler = this.listeners[eventName][i];
        handler(param);
    }
}