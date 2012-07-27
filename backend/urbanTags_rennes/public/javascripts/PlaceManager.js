// "use strict";

/**
* This class contains the place information. It manages places (adding, editing, removing) and dispatch events corresponding to actions made on places.
*
* @class PlaceManager
* @constructor
*/
function PlaceManager()
{
    CanFireEvents.call(this, ["placeAdded", "placeEdited", "placeSelected", "placeUnselected", "placeDeleted", "startLoading"]);
	this.places        = [];
	this.selectedPlace = null;
}

extend(PlaceManager.prototype, CanFireEvents.prototype);

/**
 * Load places from the server
 *
 * @method loadData
 */
PlaceManager.prototype.loadData = function()
{
    this.places = [];
    var obj     = this;
    this.fireEvent("startLoading");
    $.get(jsRoutes.getPlaceListAction(), function(data){
        jQuery.each(data, function(i, place){
            obj.addPlace(this);
        });
    });
};

/**
* Get the place which has the given name
*
* @method getPlaceByName
* @param {String} name Place's name
* @return {Object} Place which has the given name.
*/
PlaceManager.prototype.getPlaceByName = function(name)
{
    var found = false;
    var cpt   = 0;
    while(!found && cpt < this.places.length){
        found = (this.places[cpt].name === name);
        if(!found){
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
};

/**
* Add a place to the place list and fire a placeAddedEvent
*
* @method addPlace
* @param {Object} place Place to add
*/
PlaceManager.prototype.addPlace = function(place)
{
    this.places.push(place);
    this.fireEvent("placeAdded", place);
};

/**
* Edit a place and fire a placeEditedEvent
*
* @method editPlace
* @param {Object} place Edited place
*/
PlaceManager.prototype.editPlace = function(place)
{
    // Find old place
    var id       = place.id;
    var found    = false;
    var i        = 0;
    var oldPlace = null;
    while(!found  && i < this.places.length)
    {
        found = this.places[i].id == id;
        if(found){
            oldPlace = this.places[i];
        }
        i++;
    }
    
    if(found)
    {
        for(var key in place){
            oldPlace[key] = place[key];
        }
        this.fireEvent("placeEdited", oldPlace);
    }
};

/**
* Select the place which has the given name
*
* @method selectPlaceByName
* @param {String} name Place name
*/
PlaceManager.prototype.selectPlaceByName = function(name)
{
    var place = this.getPlaceByName(name);
    this.selectPlace(place);
};

/**
* Select the given place
*
* @method selectPlace
* @param {Object} place Place which must be selected.
*/
PlaceManager.prototype.selectPlace = function(place)
{
    var index = this.places.indexOf(place);
    if(index > -1)
    {
        this.selectedPlace = place;
        this.fireEvent("placeSelected", place);
    }
};

/**
* Delete the given place.
*
* @method deletePlace
* @param {Object} place Place to delete.
*/
PlaceManager.prototype.deletePlace = function(place)
{
	$.get(jsRoutes.place.remove({'id':place.id}), function(data)
	{
		console.log(data);
		var index     = this.places.indexOf(place);
		var newPlaces = [];
        var i;
		for(i = 0; i < index; i++)
		{
			newPlaces.push(this.places[i]);
		}
		
		for(i = index+1; i < this.places.length; i++)
		{
			newPlaces.push(this.places[i]);
		}
		
		this.places = newPlaces;
		if(this.selectedPlace == place)
		{
			this.unselectPlace();
		}
		
		this.fireEvent("placeDeleted", place);
	}.bind(this)).error(
        function(data){
            if(typeof data.status != "undefined" && data.status === 0){
                displayAlert("La connexion avec le serveur a été rompue.");
            }
            else{
                displayAlert("Oups, erreur inconnue.");
            }

            this.fireEvent("placeDeleted", "undefined");
        }.bind(this)
    );
};

/**
* Unselect the current selected place.
*
* @method unselectPlace
*/
PlaceManager.prototype.unselectPlace = function()
{
    this.selectedPlace = null;
    this.fireEvent("placeUnselected");
};