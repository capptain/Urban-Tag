    // "use strict";

/**
* This class manages the place list behavior.
*
* @class PlaceList
* @constructor
* @param {Object} _manager Place manager which contains place data
* @param {Object} _placeWizard Place wizard to add/edit places
*/
function PlaceList(_manager, _placeWizard) {
    this.manager = _manager;
    this.placeWizard = _placeWizard;
    this.placeItems = {};
    
    this.placeAddedRegistration = this.manager.register("placeAdded", this.onPlaceAdded.bind(this));
    this.placeSelectedRegistration = this.manager.register("placeSelected", this.onPlaceSelected.bind(this));
    this.placeEditedRegistration = this.manager.register("placeEdited", this.onPlaceEdited.bind(this));
    this.placeUnselectedRegistration = this.manager.register("placeUnselected", this.onPlaceUnselected.bind(this));
    this.placeDeletedRegistration = this.manager.register("placeDeleted", this.onPlaceDeleted.bind(this));
    this.placeLoadingRegistration = this.manager.register("startLoading", this.onLoading.bind(this));
    
    $.get(jsRoutes.place.list.view.item(), function(data)
    {
        this.templateView = data;
    }.bind(this));
    
    $("#place-list-add-button").click(function()
    {
        this.placeWizard.show();
    }.bind(this));
}

/**
 * Load places from the place manager and display them
 *
 * @method refresh
 */
PlaceList.prototype.refresh = function()
{
    this.placeItems = {};
    $("tr.place-list-item", "#place-list-table").remove();
    // Generate HTML for each place
    jQuery.each(this.manager.places, function(i, place)
    {
        var html = $(this.templateView);
        $("td.place-name", html).html(place.name);
        $("td.place-type", html).html(place.mainTag.name);
        $("#place-list-table tbody").append(html);
        
        // Store html into datastructure
        this.placeItems[place.id] = html;
        
        // Add Click handler
        var obj = this;
        html.click(function() {
            var row = $(this).parent().children("tr.place-list-item").index($(this));
            obj.manager.selectPlaceByIndex(row);
        });
    }.bind(this));
};

/**
* Add an item to the place list
*
* @method addItem
* @param {Object} place Place corresponding to the new item.
*/
PlaceList.prototype.addItem = function(place)
{
    var html = $(this.templateView);
    $("td.place-name", html).html(place.name);
    $("td.place-type", html).html($("<span class='label' style='background:#"+place.mainTag.color+";'>"+place.mainTag.name+"</span>"));
    $("#place-list-table tbody").append(html);
    
    // Store html into datastructure
    this.placeItems[place.id] = html;
    
    // Add Click handler
    var obj = this;
    html.click(function() {
        obj.manager.selectPlace(place);
    });
};

/**
* Remplace an item by another one.
*
* @method replaceItem
* @param {Object} place Place corresponding to the new item.
*/
PlaceList.prototype.replaceItem = function(place)
{
    var html = this.placeItems[place.id];
    $("td.place-name", html).html(place.name);
    $("td.place-type", html).html($("<span class='label' style='background:#"+place.mainTag.color+";'>"+place.mainTag.name+"</span>"));
};

/**
 * Handles to a placeAdded event, add the item corresponding to the added place.
 *
 * @method onPlaceAdded
 * @param {Object} place Added place.
 */
PlaceList.prototype.onPlaceAdded = function(place)
{
    hideLoader($("#place-list-container"));
    this.addItem(place);
};

/**
* Handles a placeEdited event. Replace the item of the old place by the item of the new place.
*
* @method onPlaceEdited
* @param {Object} place Edited place.
*/
PlaceList.prototype.onPlaceEdited = function(place)
{
    this.replaceItem(place);
};

/**
 * Handles a placeSelected event. Set the selected style to the item corresponding to the selected place.
 *
 * @method onPlaceSelected
 * @param {Object} place Selected place.
 */
PlaceList.prototype.onPlaceSelected = function(place)
{
    var selectedIndex = this.manager.places.indexOf(place);
    if(selectedIndex !== -1)
    {
        $('tr.place-list-item.selected', "#place-list-table").removeClass('selected');
        var row = this.placeItems[place.id];
        if(typeof row != "undefined")
        {
            row.addClass('selected');
        }
    }
    
    $("#place-list-edit-button").bind("click", function()
    {
        this.placeWizard.show(this.manager.selectedPlace);
    }.bind(this));
    
    if(place.owner.id == myId)
    {
        if(typeof $("#place-list-delete-button").attr("disabled") != "undefined")
        {
            $('#place-list-delete-button').removeAttr("disabled");
            $('#place-list-edit-button').removeAttr("disabled");
            
            $("#place-list-delete-button").bind("click", function()
            {
                showLoader($("#place-list-container .button-container"));
                this.manager.deletePlace(this.manager.selectedPlace);
            }.bind(this));
        }
    }
};

/**
* Handles a placeUnselected event. Set button's css.
*
* @method onPlaceUnselected
*/
PlaceList.prototype.onPlaceUnselected = function()
{
    $('tr.selected', "#place-list-table").removeClass('selected');
    $('#place-list-delete-button').attr("disabled", "disabled");
    $('#place-list-edit-button').attr("disabled", "disabled");
};

/**
* Handles a placeDeleted event. Remove the item corresponding to the deleted place.
*
* @method onPlaceDeleted
* @param {Object} place Deleted place.
*/
PlaceList.prototype.onPlaceDeleted = function(place)
{
    hideLoader($("#place-list-container .button-container"));
    if(typeof place != "undefined")
    {
        if(typeof this.placeItems[place.id] != "undefined")
        {
            var html = this.placeItems[place.id];
            html.remove();
            delete this.placeItems[place.id];
        }
    }
};

/**
* Handles a loading event, displays the loader.
*
* @method onLoading
*/
PlaceList.prototype.onLoading = function()
{
    showLoader($("#place-list-container"));
};