"use strict";

function PlaceList(_manager) {
    this.manager = _manager;
    
    this.placeAddedRegistration = this.manager.register("placeAdded", this.onPlaceAdded.bind(this));
    this.placeEditedRegistration = this.manager.register("placeSelected", this.onPlaceSelected.bind(this));
    this.placeUnselectedRegistration = this.manager.register("placeUnselected", this.onPlaceUnselected.bind(this));
    
    $.get(jsRoutes.place.list.view.item(), function(data)
    {
        this.templateView = data;
    }.bind(this));
}

/**
 * Load places from the server and display them
 */
PlaceList.prototype.refresh = function()
{
    $("tr.place-list-item", "#place-list-table").remove();
    // Generate HTML for each place
    jQuery.each(this.manager.places, function(i, place)
    {
        var html = $(this.templateView);
        $("td.place-name", html).html(place.name);
        $("td.place-type", html).html(place.mainTag.name);
        $("#place-list-table").append(html);
        
        // Add Click handler
        var obj = this;
        html.click(function() {
            var row = $(this).parent().children("tr.place-list-item").index($(this));
            obj.manager.selectPlaceByIndex(row);
        });
    }.bind(this));
};

PlaceList.prototype.addItem = function(place)
{
    var html = $(this.templateView);
    $("td.place-name", html).html(place.name);
    $("td.place-type", html).html(place.mainTag.name);
    $("#place-list-table").append(html);
    
    // Add Click handler
    var obj = this;
    html.click(function() {
        var row = $(this).parent().children("tr.place-list-item").index($(this));
        obj.manager.selectPlaceByIndex(row);
    });
};

/**
 * Refresh the list's display
 */
PlaceList.prototype.onPlaceAdded = function(place)
{
    this.addItem(place);
};

/**
 * Change the background of the row corresponding to the selected place 
 */

PlaceList.prototype.onPlaceSelected = function(place)
{
    var selectedIndex = this.manager.places.indexOf(place);
    if(selectedIndex !== -1)
    {
        jQuery('tr.place-list-item.selected', "#place-list-table").removeClass('selected');
        jQuery('tr.place-list-item:eq(' + selectedIndex + ')', "#place-list-table").addClass('selected');
    }
    $('#place-list-delete-button').removeClass("disabled");
    $('#place-list-edit-button').removeClass("disabled");
};

PlaceList.prototype.onPlaceUnselected = function()
{
    jQuery('tr.selected', "#place-list-table").removeClass('selected');
    $('#place-list-delete-button').addClass("disabled");
    $('#place-list-edit-button').addClass("disabled");
}