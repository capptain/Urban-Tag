(function(){
    "use strict";
})();

function PlaceList(_manager, _placeWizard) {
    this.manager = _manager;
    this.placeWizard = _placeWizard;
    this.placeItems = {};
    
    this.placeAddedRegistration = this.manager.register("placeAdded", this.onPlaceAdded.bind(this));
    this.placeSelectedRegistration = this.manager.register("placeSelected", this.onPlaceSelected.bind(this));
    this.placeEditedRegistration = this.manager.register("placeEdited", this.onPlaceEdited.bind(this));
    this.placeUnselectedRegistration = this.manager.register("placeUnselected", this.onPlaceUnselected.bind(this));
    this.placeDeletedRegistration = this.manager.register("placeDeleted", this.onPlaceDeleted.bind(this));
    
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
 * Load places from the server and display them
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

PlaceList.prototype.replaceItem = function(place)
{
    var html = this.placeItems[place.id];
    $("td.place-name", html).html(place.name);
    $("td.place-type", html).html($("<span class='label' style='background:#"+place.mainTag.color+";'>"+place.mainTag.name+"</span>"));
};

/**
 * Refresh the list's display
 */
PlaceList.prototype.onPlaceAdded = function(place)
{
    this.addItem(place);
};


PlaceList.prototype.onPlaceEdited = function(place)
{
    this.replaceItem(place);
};

/**
 * Change the background of the row corresponding to the selected place
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
                this.manager.deletePlace(this.manager.selectedPlace);
            }.bind(this));
        }
    }
};

PlaceList.prototype.onPlaceUnselected = function()
{
    $('tr.selected', "#place-list-table").removeClass('selected');
    $('#place-list-delete-button').attr("disabled", "disabled");
    $('#place-list-edit-button').attr("disabled", "disabled");
};

PlaceList.prototype.onPlaceDeleted = function(place)
{
    if(typeof this.placeItems[place.id] != "undefined")
    {
        var html = this.placeItems[place.id];
        html.remove();
        delete this.placeItems[place.id];
    }
};